package minitest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


// 서울 지역 코드를 저장하는 테이블 localcode1을 만든다(JSON->DB. 계정 root, mytest.localcode1)
// lib에 MySQL연결 파일. org.json은 Java Project 우클릭 > Properties > Java Build Path
// > Libraries > Classpath > Add External JARs > json-simple-1.1.1.jar 적용 = 프로젝트 내 문제 없.
public class LocalCode {
	public LocalCode() throws IOException, SQLException, ClassNotFoundException, ParseException {
		// API의 인증키 및 주소. 보통 StringBuilder를 사용하여 문자열을 추가한다. 
		// 새 객체를 만들어 기존 데이터에 값을 추가한다. 속도가 빠르다.
		String urlStr = "https://apis.data.go.kr/B551011/KorService1/areaCode1?serviceKey=%2F1%2FWFJ2PX6YCdyU399zkO6uBC6iCKNtuEOO3PjMNWBiBOmvZFLeGkLxoBwGRo1oBXit3oN1P8bI3Hb5fuqARog%3D%3D&numOfRows=25&pageNo=1&MobileOS=ETC&MobileApp=AppTest&areaCode=1&_type=json"; 
		URL url;
		try {
			// 문자열을 URL 클래스 객체로 만든다. 절대경로와 상대경로 2가지 방법이 있다.
			url = new URL(urlStr);
			
			// HttpURLConnection은 openConnection으로 연결한다.
			// HttpURLConnection은 URL 주소의 원격 객체에 접속한 후 통신이 가능한 URLConnection 객체를 리턴한다.
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");//GET 대문자!!주의.
			conn.setRequestProperty("Content-type", "application/json");
			// 해당 함수 실행 시 200(정상)을 출력한다.
		    System.out.println("Response code: " + conn.getResponseCode());
		    
		    if(conn.getResponseCode()==200) {
		    	 // JDBC 드라이버 클래스 이름과 JDBC URL을 알맞게 변경해주세요.
	            String jdbcUrl = "jdbc:mysql://localhost:3306/mytest";
	            String username = "root";
	            String password = "1234";
	            Class.forName("com.mysql.cj.jdbc.Driver");
	            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
	            
	            // JSON 데이터 파싱 후 DB에 저장
	    		// Buffer를 사용하면 IO할 떄 속도가 빠르다.
	            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            StringBuilder sb = new StringBuilder();
	            String line;
	            while ((line = br.readLine()) != null) {
	                sb.append(line);
	            }
	            String json = sb.toString();
	            
	            // JSON 파싱 로직 작성
	            JSONParser jsonParser = new JSONParser();
	            JSONObject jsonObject = (JSONObject) jsonParser.parse(json);  // 이 부분이 수정되었습니다.

	            // JSONArray items = (JSONArray) jsonObject.get("response").get("body").get("items").get("item");
	            JSONObject responseBody = (JSONObject) jsonObject.get("response");
	            JSONObject body = (JSONObject) responseBody.get("body");
	            JSONObject itemsObject = (JSONObject) body.get("items");
	            JSONArray items = (JSONArray) itemsObject.get("item");
	            
	            String insertQuery = "INSERT INTO localcode1 (code, name) VALUES (?, ?)";
	            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
	            for (int i = 0; i < items.size(); i++) {
	                JSONObject item = (JSONObject) items.get(i);
	                int code = Integer.parseInt((String)item.get("code"));
	                String name = (String) item.get("name");

	                preparedStatement.setInt(1, code);
	                preparedStatement.setString(2, name);
	                preparedStatement.executeUpdate();
	            }
	           
	            preparedStatement.close();
	            connection.close();

	            System.out.println("데이터베이스에 저장되었습니다.");
		    }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, ParseException {
		LocalCode lc=new LocalCode();
		
	}
}
