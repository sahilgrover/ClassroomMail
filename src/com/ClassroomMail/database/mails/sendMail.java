package com.ClassroomMail.database.mails;

import com.ClassroomMail.database.draft.*;
import com.ClassroomMail.database.utils.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class sendMail {

    public static String sendMail(String messageTimestamp, String subjectId, String subjectName, String senderMail, String receiverMail, String message, String markimportant, String source){
        Connection con = null;
        PreparedStatement stmt = null;

        String query = DBUtils.prepareInsertQuery("classroommail.mails", "messageTimestamp, subjectId, senderMail, receiverMail, message","?,?,?,?,?");

        String status = "ongoing";

        try{
            con = DBUtils.getConnection();
            stmt = con.prepareStatement(query);
            stmt.setString(1, messageTimestamp);
            stmt.setString(2, subjectId);
            stmt.setString(3, senderMail);
            stmt.setString(4, receiverMail);
            stmt.setString(5, message);
            stmt.executeUpdate();

            if (source.equals("compose")){
                newThread.saveAsDraft(subjectId,receiverMail,subjectName,markimportant,"false","false","false","","","");

                String isPresent = findUserInThread.finUserInThread(subjectId,senderMail); //whether user already present in receiver ,ail
                if (isPresent.equals("false"))
                    newThread.saveAsDraft(subjectId,senderMail,subjectName,markimportant,"false","true","false","","","");
            }
            else{       //as a reply
                //making latest message read here as false for receiver entries
                String[] emailArray = receiverMail.split(";");

                for (String mail:emailArray) {
                    if (!mail.equals(""))
                    {
                        String isUserInThread = findUserInThread.finUserInThread(subjectId,mail);
                        if (isUserInThread.equals("true")){
                            updateThread.update(subjectId,mail,"latestMessageRead", "false");
                            updateThread.update(subjectId,mail,"deleted", "false"); //if user deleted your data
                        }
                        else{
                            //checking whether mail was sent first time from draft
                            if (fetchNoOfUserInThread.fetchNoOfUserInThread(subjectId)==1)
                                newThread.saveAsDraft(subjectId,mail,subjectName,markimportant,"false","false","false","","","");
                            //Or this mail is just reply from draft in that thread
                            else if(source.equals("forward"))
                                newThread.saveAsDraft(subjectId,mail,"Fwd: "+subjectName,markimportant,"false","false","false","","","");
                            else
                                newThread.saveAsDraft(subjectId,mail,"Re: "+subjectName,markimportant,"false","false","false","","","");
                        }
                    }
                }
            }

            status="success";

        }
        catch(Exception e){
            status = e.getMessage();
        }
        finally{
            DBUtils.closeStatement(stmt);
            DBUtils.closeConnection(con);
            return status;
        }
    }
}
