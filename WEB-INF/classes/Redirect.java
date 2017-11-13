/* Default.java */

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.sql.Timestamp;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Redirect extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName() );

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String url = request.getRequestURL().toString();
        String[] tokens = url.split("/");
        String h_url = tokens[tokens.length - 1];
        LOGGER.info("\nPost /r/"+h_url);
        // config response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // response json container
        Map<String, String> resContainer = new LinkedHashMap<String, String>();

        // for fast access "fake db", this is a hack, fetch on every get since
        // it might be updated in another route
        List<String> jsonStore = new ArrayList<>();
        try{
            String jsonEntryStr = "";
            // loop thru "DB"
            File file = new File(getServletContext().getRealPath("/") + "fake.db.json");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((jsonEntryStr = bufferedReader.readLine()) != null) {
                if (jsonEntryStr.length()>0 && jsonEntryStr.charAt(0)==','){
                    jsonEntryStr = jsonEntryStr.substring(1, jsonEntryStr.length());
                }
                jsonStore.add(jsonEntryStr);
            }
            fileReader.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }


        // Redirct logic, since there is no real user system in the application,
        // no functionallity for checking the url can be only viewed by a spcific user.

        // will only check the expire time here.
        // if the hash is not expired and recored in the "DB", allow direct, otherwise,
        // print error message
        boolean record_found = false;
        for (int i =0; i < jsonStore.size() && !record_found;i++){
            String fakeRecord = jsonStore.get(i);

            String pattern = "\"h_url\":\"(.*)\"";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(fakeRecord);
            if (m.find()) {
                String temp_h_url = m.group(1);
                LOGGER.info("\nh_url: "+h_url+" r_url: "+temp_h_url);
                if(new String(temp_h_url).equals(h_url)){ // matched hash in DB
                    String time_stamp_pattern = "\"expire_time\":\"(.*)\"";
                    Pattern tr = Pattern.compile(time_stamp_pattern);
                    Matcher time_match = tr.matcher(fakeRecord);
                    if (time_match.find()){
                        record_found = true;
                        LOGGER.info("Found matched hash url in DB");
                        String expire_time_str = time_match.group(1);
                        // form a time stamp object
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                            Date parsedDate = dateFormat.parse(expire_time_str);
                            Timestamp expire_time_timestamp = new java.sql.Timestamp(parsedDate.getTime());

                            Date date = new Date();
                            Timestamp now_timestamp = new Timestamp(date.getTime());

                            if (now_timestamp.after(expire_time_timestamp)){ // if the link already expired
                                LOGGER.info("Url already expired. Redircting... "+request.getRequestURI());
                                response.setContentType("text/html");
                                response.sendRedirect("/test1/expired.html");
                            }else{
                                String o_url_pattern = "\"o_url\":\"(.*)\"";


                                Pattern ur = Pattern.compile(o_url_pattern);
                                Matcher o_url_match = ur.matcher(fakeRecord);
                                if (o_url_match.find()) {
                                    String o_url =  java.net.URLDecoder.decode(o_url_match.group(1).substring(0, o_url_match.group(1).indexOf("\"")), "UTF-8");
                                    LOGGER.info("Hash check passed, ready to redirect to " + o_url);
                                    response.setContentType("text/html");
                                    response.sendRedirect(o_url);
                                }else{
                                    LOGGER.info("Can't find the original url in the record.");
                                }
                            }
                        } catch(Exception e) { //this generic but you can control another types of exception
                            e.printStackTrace();
                        }
                    }
                }

            }
        }

        // invalid hash link
        if (!record_found) {
            response.setContentType("text/html");
            LOGGER.info(request.getRequestURL().toString());
            response.sendRedirect("http://localhost:3000/test1/404.html");
        }


    }

}