 package servlets;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONObject;

import dataBase.MysqlDataBaseConnection;
import hash.Hashing;
import jjwt.JWT;

@MultipartConfig
@WebServlet("/login")
public class Login extends HttpServlet{
	private static final long serialVersionUID = 1L;

	
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {        
        try {
            
			StringBuilder requestBody = new StringBuilder();
	        BufferedReader reader = req.getReader();
	        String line;
	        while ((line = reader.readLine()) != null) {
	        	requestBody.append(line);
	        }
	        JSONObject json = new JSONObject(requestBody.toString());
	        String userMobileNumber = json.optString("mobileNumber");
	        
			Connection con=MysqlDataBaseConnection.getMysqlConnection();
			PreparedStatement pStmt=con.prepareStatement("select password,salt from users where mobileNumber=?");
			ResultSet resultSet=null;
			
			if(userMobileNumber.length()==10 && json.optString("password")!="") {
				pStmt.setString(1, userMobileNumber);
				resultSet= pStmt.executeQuery();
				
				if(resultSet.next()){
					String hashedPassword=Hashing.bytesToHex(Hashing.hashPassword(Hashing.combineSaltAndPassword(json.optString("password"), resultSet.getBytes("salt"))));
					if(hashedPassword.equals(resultSet.getString("password"))){
						res.getWriter().write(JWT.generateToken(hashedPassword ,userMobileNumber));
						res.setStatus(HttpServletResponse.SC_OK);
					}else {
						res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					}
				}else {
					res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				}
			}else {
				res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}
			
			
		}catch(Exception err) {
			err.printStackTrace();
			System.out.println(err);
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			
		}
    }
	
}
