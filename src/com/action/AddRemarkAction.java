package com.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Created by lang.liu on 2016/12/29.
 */
public class AddRemarkAction extends AnAction {

    private String title;
    private String language;
    private Project mProject;
    Map<String,Properties> pros = new HashMap<String, Properties>();

//    private Properties remarkProperties;
    /**项目数据文件绝对路径*/
//    private String proPath;
//    /**当前项目名称*/
//    String proName;


    @Override
    public void update(AnActionEvent event)
    {
        //根据文件类型，显示隐藏此Action
        boolean isDirectory = isDirectory(event.getDataContext());
        this.getTemplatePresentation().setEnabled(isDirectory);

        title = "警告";
        String menuName = "新增备注";
        Locale locale = Locale.getDefault();
        language = locale.getLanguage();
        if(!"zh".equals(language))
        {
            menuName = "create Note";
            title = "Waring";
        }

        menuName = "文件夹备注";
        if(!"zh".equals(language))
        {
            menuName = "Folder notes";
        }
        this.getTemplatePresentation().setText(menuName);
    }


    public void actionPerformed(AnActionEvent event)
    {
        //getRemarkProperties(DataKeys.VIRTUAL_FILE.getData(event.getDataContext()));
        mProject = event.getData(PlatformDataKeys.PROJECT);
        DataContext dataContext = event.getDataContext();
        if (!isDirectory(dataContext))
        {
            String info = "仅可以给文件夹设置备注！";
            if(!"zh".equals(language))
            {
                info = "You can only set notes to folders!";
            }
            Messages.showMessageDialog(
                    info, title ,
                    Messages.getInformationIcon());
            return;
        }
        String remarkValue = askForName(mProject,event);
        if(remarkValue == null)
        {
            return;
        }

        if(remarkValue.length() > 200)
        {
            String info = "备注长度不能大于200，请重新输入！";
            if(!"zh".equals(language))
            {
                info = "Note length should not be greater than 200!";
            }
            Messages.showMessageDialog(
                    info, title,
                    Messages.getInformationIcon());
            return;
        }

        if (isDirectory(dataContext))
        {
        //根据扩展名判定是否进行下面的处理
            //获取选中的文件
            VirtualFile file = DataKeys.VIRTUAL_FILE.getData(event.getDataContext());
            if (file != null)
            {
                try {
                    boolean result = saveMap(file,remarkValue);
                    if(!result)
                    {
                        String info = "保存失败！";
                        if(!"zh".equals(language))
                        {
                            info = "save failed!";
                        }
                        Messages.showMessageDialog(mProject, info, title, Messages.getInformationIcon());
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }


    public static void main(String[] args) throws Exception {
        Properties remarkProperties = new Properties();
        FileInputStream in = new FileInputStream(new File("E:\\ideaPlugin\\.idea\\remark.properties"));
        remarkProperties.load(in);
//        System.out.println(remarkProperties.containsKey(null));
//        System.out.println(remarkProperties.getProperty(null));
        System.out.println(remarkProperties.getProperty(null));

    }

    /**
     * 获取备注
     * @param project
     * @return
     */
    private String askForName(Project project,AnActionEvent event)
    {
        String remarkFilePath = getFileRelativePath(DataKeys.VIRTUAL_FILE.getData(event.getDataContext()));
        if(remarkFilePath == null)
            return null;
        String oldRemark = "";
        Properties remarkProperties = getRemarkProperties(DataKeys.VIRTUAL_FILE.getData(event.getDataContext()));
        if(remarkProperties.containsKey(remarkFilePath) && remarkProperties.getProperty(remarkFilePath) != null && !"".equals(remarkProperties.getProperty(remarkFilePath)))
        {
            String info = "此文件夹旧的备注：";
            if(!"zh".equals(language))
            {
                info = "old note:";
            }
            oldRemark = info + remarkProperties.getProperty(remarkFilePath);
        }


        String info = "请输入文件夹备注：";
        if(!"zh".equals(language))
        {
            info = "Please enter a folder note:";
        }
        return Messages.showInputDialog(project,
                oldRemark, info,
                Messages.getQuestionIcon());
    }

    /**
     * 判断文件类型是否是目录，如果是则返回true
     * @param dataContext
     * @return
     */
    private boolean isDirectory(DataContext dataContext)
    {
        if(dataContext == null)
            return false;
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(dataContext);
        if(file == null)
        {
            return false;
        }
        return file.isDirectory();
    }

    /**
     * 存储keymap
     */
    private boolean saveMap(VirtualFile file,String remarkValue)throws Exception
    {
        String remarkFilePath = getFileRelativePath(file);
        if(remarkFilePath == null)
            return false;

        //key 存项目根路径到该文件的路径
        OutputStream oFile = new FileOutputStream(getProPath(file));
        getRemarkProperties(file).setProperty(remarkFilePath, remarkValue);
        getRemarkProperties(file).store(oFile, "Update '" + remarkFilePath + "' value");
        oFile.close();
        return true;
    }

    /**
     * 获取当前选中文件的相对路径：从项目名到该文件的路径
     */
    private String getFileRelativePath(VirtualFile file)
    {
        String rootPath = file.getPath();
        int endIndex = rootPath.indexOf("/src/");
        if(endIndex == -1)
        {
            String info = "此版本仅支持src子目录添加备注！";
            if(!"zh".equals(language))
            {
                info = "This version supports only the src subdirectory!";
            }
            Messages.showMessageDialog(mProject, info, title, Messages.getInformationIcon());
            return null;
        }

        String tempRootPath = rootPath.substring(0,endIndex);
        int startIndex = tempRootPath.lastIndexOf("/") + 1;
        String remarkFilePath = rootPath.substring(startIndex);
        return remarkFilePath;
    }





    /**
     * 获取当前项目名称
     */
    private String getProName(VirtualFile file)
    {
        String rootPath = file.getPath();
        int endIndex = rootPath.indexOf("/src/");
        String tempRootPath = rootPath.substring(0,endIndex);
        int startIndex = tempRootPath.lastIndexOf("/") + 1;
//        proName =  rootPath.substring(startIndex,endIndex);//项目名称
        return rootPath.substring(startIndex,endIndex);//项目名称
    }

    /**
     * 获取 项目数据文件绝对路径
     * @param file
     */
    private String getProPath(VirtualFile file)
    {
        String rootPath = file.getPath();
        int endIndex = rootPath.indexOf("/src/");
//        String proName =  rootPath.substring(startIndex,endIndex);//项目名称

        StringBuffer proPathTemp = new StringBuffer(rootPath.substring(0, endIndex));//项目绝对路径

        proPathTemp.append("/.idea");
        File proFile = new File(proPathTemp.toString());
        if(!proFile.exists())
        {
            proFile.mkdirs();
        }
        proPathTemp.append("/remark.properties");
        return proPathTemp.toString();
    }


    /**
     * 如果remarkProperties为空则设置它
     */
    private Properties getRemarkProperties(VirtualFile file)
    {
        try {
            File mapFile = new File(getProPath(file));
            if(!mapFile.exists())
            {
                mapFile.createNewFile();
            }

            //判断map是否有，如果有则直接用，没有就创建
            String proName = getProName(file);
            if(pros.containsKey(proName))
            {
                return pros.get(proName);
            }else
            {
                Properties remarkProperties = new Properties();
                FileInputStream in = new FileInputStream(mapFile);
                remarkProperties.load(in);
                pros.put(proName,remarkProperties);
                return pros.get(proName);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
