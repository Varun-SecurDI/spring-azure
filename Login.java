package com.spring.azure.controller;

import javax.net.ssl.*;
import java.net.*;
import java.sql.*;
import java.io.*;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Login {

	public String accessToken;
	public JSONObject json;
	public Object email;
	public Timestamp otpstamp;
	public String pas;
	public static final int ovt=3*60*1000;
	public String pass;
	public JSONObject json2;
	public JSONObject json4;
	public JSONObject json7;
	public String user;
	public Object skey;
	public String cusprop;
	public Object customproperty10;
	public Object username;
	public Object cp20;
	public Object cp_20;
    public int attempt;
    public int attempts;

	
	@GetMapping("/accessclaim")
	public String accesstoken() throws IOException
	{
		URL url = new URL("https://securdi-partner.saviyntcloud.com/ECM/api/login");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        
        String body = "{\"username\":\"vambrale\",\"password\":\"V@run95?\"}";
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(body);
        outputStream.flush();
        outputStream.close();
        //get status code like 200/404/401  etc  
        int responseCode = connection.getResponseCode();
        System.out.println("Response code for Access Token Generation : " + responseCode);
        // to read the access token complete body
        InputStream inputStream = connection.getInputStream();
        Scanner scanner = new Scanner(inputStream);
        String responseBody = scanner.useDelimiter("\\A").next();
        System.out.println("Response body: " + responseBody);
        //to read only the access token from the body
        JSONObject jsonObject = new JSONObject(responseBody);
        accessToken = jsonObject.getString("access_token");
        System.out.println("Access Token: " + accessToken);
                           
        // Close the connection
        scanner.close();
        connection.disconnect();
        
        return "accessclaim";
		
	}
	
	@GetMapping("/generate")
		
	public String generateotp(ModelMap model, @RequestParam String uname, @RequestParam String cp10) throws IOException
	{
		user=uname;
		cusprop=cp10;    		
		URL url1 = new URL("https://securdi-partner.saviyntcloud.com/ECM/api/getUser");
        HttpsURLConnection connection1 = (HttpsURLConnection) url1.openConnection();
        connection1.setRequestMethod("POST");
        connection1.setDoOutput(true);
        connection1.setRequestProperty("Authorization","Bearer " + accessToken);
        connection1.setRequestProperty("Content-Type", "application/json");
        String body1 ="{\n\"filtercriteria\":{\"username\":\""+uname+"\",\"customproperty10\":\""+cp10+"\"}\n}";
        DataOutputStream outputStream1 = new DataOutputStream(connection1.getOutputStream());
        outputStream1.writeBytes(body1);
        outputStream1.flush();
        outputStream1.close();
        int responseCode1 = connection1.getResponseCode();
        System.out.println("Response code for User Test Generation : " + responseCode1);

        //only fetch result parameter from the entire report
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection1.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        System.out.println("Result for user details: "+sb.toString());
        connection1.disconnect();

        json = new JSONObject(sb.toString());
        JSONArray json1 = json.getJSONArray("userlist");
        for(int i=0;i<json1.length();i++) {
        	String result=json1.getString(i);
        	JSONObject jsonObject1 = new JSONObject(result);
            email = jsonObject1.get("email");
            System.out.println("Email: "+email);
            username=jsonObject1.get("username");
            customproperty10=jsonObject1.get("customproperty10");
            skey=jsonObject1.get("statuskey");
    	}
        if (uname.equals(username) && cp10.equals(customproperty10)) 
        {
        	if (json.toString().contains("\"customeproperty65\":null")) {
        		
            if(email!=null) {
            final String CHARACTER = "0123456789";
            Random random = new Random();
            StringBuilder password = new StringBuilder();
            for (int i = 0; i < 5; i++)
            {
                int index = random.nextInt(CHARACTER.length());
                password.append(CHARACTER.charAt(index));
                pas=password.toString();
            }
            otpstamp = new Timestamp(System.currentTimeMillis());
            }
            
            
        URL url2 = new URL("https://securdi-partner.saviyntcloud.com/ECM/api/sendEmail");
        Map<String,Object> param = new LinkedHashMap<>();
        param.put("to", email);
        param.put("from", "aahmed@securdi.com");
        param.put("body","Hi "+ uname + ","+"\n\n"+"Your OTP is: "+pas+", It will be valid for 3 minutes. Go back to the portal and enter the OTP before the time runs out" );
        param.put("subject", "OTP generation email");
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> para : param.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(para.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(para.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
        HttpsURLConnection connection2 = (HttpsURLConnection) url2.openConnection();
        connection2.setRequestMethod("POST");
        connection2.setDoOutput(true);
        connection2.setRequestProperty("Authorization","Bearer " + accessToken);
        connection2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection2.setRequestProperty( "charset", "utf-8");
        connection2.setUseCaches( true );
        connection2.getOutputStream().write(postDataBytes);
        
        int responseCode2 = connection2.getResponseCode();
        System.out.println("Response code for email sending : " + responseCode2); 
        
        
        BufferedReader in = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
        	              	
           response.append(inputLine);
        }
        System.out.println("Email Sending Response " +response );
        in.close();
        connection2.disconnect();
        	}
        	else 
        	{
        		model.put("errorMsg", "Your password has already been generated");
        	    return "accessclaim";
        	}
	}
		else
	{
		model.put("errorMsg", "Invalid Credntials");
		return "accessclaim";
	}
		return "generate";
	}

	@GetMapping("/validate")
	
	public String validateotp(ModelMap model, @RequestParam String otp) throws IOException
	{
		 
			
		//validate otp and generate password 	
		if (otp!=pas) {
			
            URL url7 = new URL("https://securdi-partner.saviyntcloud.com/ECM/api/getUser");
	        HttpsURLConnection connection7= (HttpsURLConnection) url7.openConnection();
	        connection7.setRequestMethod("POST");
	        connection7.setDoOutput(true);
	        connection7.setRequestProperty("Authorization","Bearer " + accessToken);
	        connection7.setRequestProperty("Content-Type", "application/json");
	        String body7 ="{\r\n\"filtercriteria\":{\"username\":\""+user+"\"},\r\n\"responsefields\":[\"customproperty20\"]\r\n\r\n}";
	        DataOutputStream outputStream7 = new DataOutputStream(connection7.getOutputStream());
	        outputStream7.writeBytes(body7);
	        outputStream7.flush();
	        outputStream7.close();
	        int responseCode7 = connection7.getResponseCode();
	        System.out.println("Response code for User Test Generation : " + responseCode7);

	        //only fetch result parameter from the entire report
	        BufferedReader bufferedReader6 = new BufferedReader(new InputStreamReader(connection7.getInputStream()));
	        StringBuilder sb3 = new StringBuilder();
	        String line3;
	        while ((line3 = bufferedReader6.readLine()) != null) {
	            sb3.append(line3);
	        }
	        System.out.println("Result for user details: "+sb3.toString());
	        connection7.disconnect();
	        json7 = new JSONObject(sb3.toString());
	        JSONArray json6 = json7.getJSONArray("userlist");
	        for(int l=0;l<json6.length();l++) {
	        	String result2=json6.getString(l);
	        	JSONObject jsonObject4 = new JSONObject(result2);
	            cp20 = jsonObject4.get("customproperty20");
	            System.out.println("number of attempts: "+cp20);
	            attempt=Integer.valueOf((String)cp20);
	            System.out.println("number of attempted: "+attempt);
	        }			
	        int ip=attempt+1;
	        
				System.out.println("Number of tries: "+ip);
				
				
				//updating user value cp20 with number of tries
				URL url5 = new URL("https://securdi-partner.saviyntcloud.com/ECM/api/updateUser");
	     		HttpsURLConnection connection5 = (HttpsURLConnection) url5.openConnection();
	     		connection5.setRequestMethod("POST");
	     		connection5.setDoOutput(true);
	     		connection5.setRequestProperty("Authorization","Bearer " + accessToken);
	            connection5.setRequestProperty("Content-Type", "application/json");
	            String body5="{\n\"username\": \""+user+"\",\n    \"customproperty20\" : \""+ip+"\"\n}";
	            DataOutputStream outputStream5 = new DataOutputStream(connection5.getOutputStream());
	            outputStream5.writeBytes(body5);
	            outputStream5.flush();
	            outputStream5.close();
	            int responseCode5 = connection5.getResponseCode();
	            System.out.println("Response code for User Test Generation : " + responseCode5);
	             
	            InputStream inputStream5 = connection5.getInputStream();
	            Scanner scanner5 = new Scanner(inputStream5);
	            String RsBody1 = scanner5.useDelimiter("\\A").next();
	            System.out.println("Response body: " + RsBody1);
	            scanner5.close();
	            connection5.disconnect();

	            //calling the user details with cp20
	            URL url6 = new URL("https://securdi-partner.saviyntcloud.com/ECM/api/getUser");
		        HttpsURLConnection connection6= (HttpsURLConnection) url6.openConnection();
		        connection6.setRequestMethod("POST");
		        connection6.setDoOutput(true);
		        connection6.setRequestProperty("Authorization","Bearer " + accessToken);
		        connection6.setRequestProperty("Content-Type", "application/json");
		        String body6 ="{\r\n\"filtercriteria\":{\"username\":\""+user+"\"},\r\n\"responsefields\":[\"customproperty20\"]\r\n\r\n}";
		        DataOutputStream outputStream6 = new DataOutputStream(connection6.getOutputStream());
		        outputStream6.writeBytes(body6);
		        outputStream6.flush();
		        outputStream6.close();
		        int responseCode6 = connection6.getResponseCode();
		        System.out.println("Response code for User Test Generation : " + responseCode6);

		        //only fetch result parameter from the entire report
		        BufferedReader bufferedReader5 = new BufferedReader(new InputStreamReader(connection6.getInputStream()));
		        StringBuilder sb1 = new StringBuilder();
		        String line1;
		        while ((line1 = bufferedReader5.readLine()) != null) {
		            sb1.append(line1);
		        }
		        System.out.println("Result for user details: "+sb1.toString());
		        connection6.disconnect();
		        json4 = new JSONObject(sb1.toString());
		        JSONArray json5 = json4.getJSONArray("userlist");
		        for(int k=0;k<json5.length();k++) {
		        	String result1=json5.getString(k);
		        	JSONObject jsonObject3 = new JSONObject(result1);
		            cp_20 = jsonObject3.get("customproperty20");
		            System.out.println("number of attempts: "+cp_20);
		            attempts=Integer.valueOf((String)cp_20);
		            System.out.println("Number of last attempts:  "+attempts);
		           } 
		        
		      //check counter if tries more than 3 disable user 
	            if (attempts>=3) {
	            	
					model.put("errorMsg", "Login Attempts Exceeded. Your Account has been Locked");
					return "accessclaim";
					
	            }
			}


			if (pas.contains(otp)) {
				if(attempts<=3) {

            long td=System.currentTimeMillis()-otpstamp.getTime();
			if(td<=ovt) {
	 			 if (json.toString().contains("\"customeproperty65\":null") && json.toString().contains("\"msg\":\"Successful\"")) 
	         	{
	         	    
	         		final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	                 Random random = new Random();
	                 StringBuilder password = new StringBuilder();
	                 for (int j = 0; j < 12; j++)
	                 {
	                     int index = random.nextInt(CHARACTERS.length());
	                     password.append(CHARACTERS.charAt(index));
	                     pass=password.toString();  
	                 }
	                 model.put("password", pass);
	                 model.put("uname", user);
	                 model.put("cp10", cusprop);
	         	}
			}
			else {
        		model.put("errorMsg","Time Out. Please Start Again");
        		return "generate";
			}
		}
				else {
					model.put("errorMsg", "Login Attempts Exceeded");
					return "accessclaim";
		
	}
			}
		
			else {
				model.put("errorMsg", "OTP does not match");
				return "generate";
			}
		
	
		if (pas.contains(otp) && json.toString().contains("\"customeproperty65\":null") && json.toString().contains("\"msg\":\"Successful\"")) {
		
			//when password is generated for the first time 
			URL url3 = new URL("https://securdi-partner.saviyntcloud.com/ECM/api/changePassword");
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("username", user);
            params.put("password",pass);
            params.put("changePasswordAssociatedAccounts","false");
            params.put("updateUserPassword","true");
            params.put("setarstasksource","false");
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            HttpsURLConnection connection3 = (HttpsURLConnection) url3.openConnection();
    		connection3.setRequestMethod("POST");
    		connection3.setDoOutput(true);
    		connection3.setRequestProperty("Authorization","Bearer " + accessToken);
            connection3.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection3.setRequestProperty( "charset", "utf-8");
            connection3.setUseCaches( true );
            connection3.getOutputStream().write(postDataBytes);
            
            int responseCode3 = connection3.getResponseCode();
            System.out.println("Password Response for user : " + responseCode3); 
            
            
            BufferedReader in = new BufferedReader(new InputStreamReader(connection3.getInputStream()));
            String inputLine1;
            StringBuilder response3 = new StringBuilder();
            while ((inputLine1 = in.readLine()) != null) {
            	              	
               response3.append(inputLine1);
            }
            System.out.println("Reset Password " +response3 );
            json2 = new JSONObject(response3.toString());
            System.out.println("Response generated: "+json2);
		}
            
//      if password is not empty and response generated above is successful then only run the below code 
     	if (pass!=null && json2.toString().contains("Password Updated Successfully"))
     	{
     		URL url4 = new URL("https://securdi-partner.saviyntcloud.com/ECM/api/updateUser");
     		HttpsURLConnection connection4 = (HttpsURLConnection) url4.openConnection();
     		connection4.setRequestMethod("POST");
     		connection4.setDoOutput(true);
     		connection4.setRequestProperty("Authorization","Bearer " + accessToken);
             connection4.setRequestProperty("Content-Type", "application/json");
             String body4="{\n\"username\": \""+user+"\",\n    \"customproperty65\" : \"Your password has already been generated\"\n}";
             DataOutputStream outputStream4 = new DataOutputStream(connection4.getOutputStream());
             outputStream4.writeBytes(body4);
             outputStream4.flush();
             outputStream4.close();
             int responseCode4 = connection4.getResponseCode();
             System.out.println("Response code for User Test Generation : " + responseCode4);
             
             InputStream inputStream4 = connection4.getInputStream();
             Scanner scanner4 = new Scanner(inputStream4);
             String RsBody = scanner4.useDelimiter("\\A").next();
             System.out.println("Response body: " + RsBody);
             scanner4.close();
             connection4.disconnect();
     	}
		return "validate";
		}
}
