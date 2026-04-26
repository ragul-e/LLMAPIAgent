package agents;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.InvocationContext;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;


public class ApiTestingAgent {

	private static String USER_ID = "Ragul's Agent";
	private static String NAME = "Ragul's Api_Test_Agent";

    // The run your agent with Dev UI, the ROOT_AGENT should be a global public static variable.
    public static BaseAgent ROOT_AGENT = initAgent();
    
    static Response response;

public static BaseAgent initAgent() {
		
		return LlmAgent.builder()
				.name(NAME)
				.model("gemini-2.0-flash")
				.description("Agent to answer questions and print the response retrived using the endpoint.")
				.instruction("You are a helpfull agent who can anser user auestions about the endponts and its response ")
				.instruction("help user for response validation")
				.tools(FunctionTool.create(ApiTestingAgent.class, "getRequest"),
						FunctionTool.create(ApiTestingAgent.class, "basicAuth"),
						FunctionTool.create(ApiTestingAgent.class, "bearerAuthentication"),
						FunctionTool.create(ApiTestingAgent.class, "validateBody"),
						FunctionTool.create(ApiTestingAgent.class, "httpPostRequest")
						)
				.build();
		
	}

public static Map<String, String> bearerAuthentication(@Schema(description = "This function perform bearer token authentication and print"
		+ "the response body and status code with help of user promt token and endpoint suggest to create token") 
String bearertoken, String endpoint) {
	String token = bearertoken;
	
	response = RestAssured
	.given()
	.relaxedHTTPSValidation()
	.contentType(ContentType.JSON)
	.header("Authorization:","Bearer "+token)
	.when()
	.get(endpoint);
	response.prettyPrint();
	
	int statuscode = response.statusCode();
	
	if(response.statusCode()>=200 && response.statusCode()<=300) {
		return Map.of("status","success","report response body is"+ response.prettyPrint(),
				"report status code for bearer authentication is " + statuscode);
	}
	
	else {
		return Map.of("status","failure","report response body is"+ response.prettyPrint(),
				"report status code for bearer authentication is " + statuscode);
	}
	
}



public static Map<String, String> basicAuth(@Schema(description = "This function perform basic authenticatoion and print"
		+ "the response body, status with help of user promt username, password and endpoint suggest to create token") 
String username, String password, String endpoint) {
	String url  = endpoint;
	
	response = RestAssured
	.given()
	.relaxedHTTPSValidation()
	.contentType(ContentType.JSON)
	.auth()
	.basic(username, password)
	.when()
	.get(url);
	response.prettyPrint();
	
	int statuscode = response.statusCode();
	
	if(response.statusCode()>=200 && response.statusCode()<=300) {
		return Map.of("status","success","report response body is"+ response.prettyPrint(),
				"report status code for bearer authentication is " + statuscode);
	}
	
	else {
		return Map.of("status","failure","please check"+ username + "/" +password+"/"+endpoint, "is correct");
	}
	
}


public static Map<String, String> Authentication(@Schema(description = "This function perform  authenticatoion and print"
		+ "the response body, status with help of user promt username, password and endpoint suggest to create token") 
String username, String password, String endpoint) {
	String url  = endpoint;
	
	response  = RestAssured
	.given()
	.relaxedHTTPSValidation()
	.contentType(ContentType.JSON)
	.auth()
	.basic(username, password)
	.when()
	.post(url);
	response.prettyPrint();
	
	int statuscode = response.statusCode();
	
	if(statuscode>=200 && statuscode<=300) {
		return Map.of("status","success","report response body is"+ response.prettyPrint(),
				"report status code for bearer authentication is " + statuscode);
	}
	
	else {
		return Map.of("status","failure","please check"+ username + "/" +password+"/"+endpoint, "is correct");
	}
	
}


public static Map<String, String> validateBody(@Schema(description = "This method will print and validate dtring data type the value in the retrieved response body stored in the response obejct")
String userexpectedvalue){

	String value = userexpectedvalue;
	
	value = Normalizer.normalize(userexpectedvalue, Normalizer.Form.NFD).trim().replaceAll("(\\p{Ism}+|\\p{IsP}+)", "").replaceAll("\\s+","");
	
	response.then().body(hasToString(value));
	
	if(response.statusCode()>=200 && response.statusCode()<=300) {
		return Map.of("status","success","report", "response body is"+ response.prettyPrint());
	}else {
		return Map.of("status","error","report","sorry,please endpoint is correct");
	}
	
}

public static Map<String, String> validateBody(@Schema(description = "This method will print and validate numbers form retrieved response body from response objec and user will provide expected number")
String userexpectedvalue, int userexpectecvalue){

	int attribute = userexpectecvalue;
	String parsedattribute = String.valueOf(attribute);
	
	String value = Normalizer.normalize(userexpectedvalue, Normalizer.Form.NFD).trim().replaceAll("(\\p{Ism}+|\\p{IsP}+)", "").replaceAll("\\s+","");
	
	
	int userinput = Integer.parseInt(userexpectedvalue);
	response.then().body(equalTo(userinput));
	
	if(response.statusCode()>=200 && response.statusCode()<=300) {
		return Map.of("status","success","report" ,"response body is"+ response.prettyPrint());
	}else {
		return Map.of("status","error","report","please check endpoint is correct");
	}
			
}

public static Map<String, String> validateBodyString(@Schema(description = "This method will print and validate String form retrieved response body from response objec and user will provide expected string value")
String expectedvalue){

	String parsedattribute = String.valueOf(expectedvalue);
	
	String value = Normalizer.normalize(parsedattribute, Normalizer.Form.NFD).trim().replaceAll("(\\p{Ism}+|\\p{IsP}+)", "").replaceAll("\\s+","");
	
	
	response.then().body(equalTo(parsedattribute));
	
	if(response.statusCode()>=200 && response.statusCode()<=300) {
		return Map.of("status","success","report" ,"response body is"+ response.prettyPrint());
	}else {
		return Map.of("status","error","report","please check endpoint is correct");
	}
			
}


public static Map<String, String> validateBody(@Schema(description = "this tool validate multiples values stored in response obejct ")
List<String> userexpvalue){
	
	List<String> attributes = new ArrayList<String>();
	
	for(String attribute : userexpvalue) {
		attributes.add(attribute);
	}
	
	
	if(response.statusCode()>=200 && response.statusCode()<=300) {
		return Map.of("status","success","report" ,"response body is"+ response.prettyPrint());
	}else {
		return Map.of("status","error","report","please check endpoint is correct");
	}
}


	
	
//	

public static Map<String, String> getRequest(
		@Schema(description = "this tool can retrieve and print the response body for HTTP get request endpoint, user provide the endpoint")String endpoint
	) {
	
	String url = endpoint;
	
	response = RestAssured
	.given()
	.relaxedHTTPSValidation()
	.contentType(ContentType.JSON)
	.when()
	.get(url);
	
	if(response.statusCode()>=200 && response.statusCode()<=300) {
		return Map.of("status","success","report" ,"response body is"+ response.prettyPrint());
	}else {
		return Map.of("status","error","repor","please check endpoint is correct");
	}
	
	
}

    public static void main(String[] args) throws Exception {
        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);

        Session session =
            runner
                .sessionService()
                .createSession(NAME, USER_ID)
                .blockingGet();

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.print("\nYou > ");
                String userInput = scanner.nextLine();

                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }

                Content userMsg = Content.fromParts(Part.fromText(userInput));
                Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMsg);

                System.out.print("\nAgent > ");
                events.blockingForEach(event -> System.out.println(event.stringifyContent()));
            }
        }
    }
}

