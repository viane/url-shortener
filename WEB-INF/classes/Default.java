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

public class Default extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName() );

    // for fast access "fake db", this is a hack
    private List<String> jsonStore = new ArrayList<>();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        jsonStore.clear();
        // init stored urls in to memory for dirct access due to technicial issue connecting
        // DBs on my workstation
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

        try{
            // get all entries and form an array of json strings
            String json = "[";
            if (jsonStore.size()>0){
                json = json + jsonStore.get(0);

                for (int i = 1; i < jsonStore.size();i++){
                    json = json + "," + jsonStore.get(i);
                }
            }
            json = json + "]";

            response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

        }finally {
            LOGGER.info("\nGET /v1");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // config response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // pretty json string
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // response json container
        Map<String, String> resContainer = new LinkedHashMap<String, String>();
        try {

            String req_body = request.getReader().lines().collect(Collectors.joining());

            String[] params = req_body.split("&");
            
            String original_url = params[0];

            String owner =  params[3];

            String level =  params[4];


            // url create time and expiretime
            int expire = Integer.parseInt(params[2]);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, expire);
            Timestamp expire_time = new Timestamp(calendar.getTimeInMillis());

            // url private
            String url_private = params[1];
            boolean is_private = false;
            if (url_private=="1"){
                is_private = true;
            }

            // form psudo document in mongodb


            Map<String, String> URL = new LinkedHashMap<String, String>();
            URL.put("o_url", original_url);
            URL.put("owner", owner);
            URL.put("is_private", url_private.toString());
            URL.put("create_time", now.toString());
            URL.put("expire_time", expire_time.toString());
            URL.put("level",level);

            // Hash
            String h_url;

            // check duplicate hash, hack for DB, this is diaster....
            boolean duplicate_h_url = false;
            int iteration_count = 0;
            do{
                try {
                    Thread.sleep(10);
                }catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                Hash h = new Hash(owner,level,original_url,now.toString(),jsonStore.size(),jsonStore);
                h_url=h.getHashUrl();
                LOGGER.info("\nPOST /v1 processing generated hash url: "+h_url +"...");
                for (String urlObjStr : jsonStore) {  // check duplicates
                    String pattern = "\"h_url\":\"(.*)\"";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(urlObjStr);
                    if (m.find()) {
                        if(new String(m.group(1)).equals(h_url)){
                            LOGGER.info("\nDuplicate hash url found " + m.group(1) + " ---> regenerate");
                            duplicate_h_url= true;
                        }
                    }
                }
                if (iteration_count>500){
                    break;
                }
                iteration_count++;
            }while(h_url.length()==0 && duplicate_h_url);

            // failed to gerate url somehow
            if (h_url.length()==0){
                response.setStatus(400);
                resContainer.put("message", "Process time out, try again!");
                String res = new Gson().toJson(resContainer);
                response.getWriter().write(res);
            }else{
                URL.put("h_url",h_url);

                // write to "DB" and send response
                String json = new Gson().toJson(URL); // stringify object
                String result = writeToFakeDB(json);
                if (result != null){  // exception raised
                    response.setStatus(400);
                    resContainer.put("message", result);
                }else{
                    response.setStatus(200);
                    resContainer.put("message", "success.");
                    // update Memory
                    jsonStore.add(json);
                }


                String res = new Gson().toJson(resContainer);
                response.getWriter().write(res);
            }



        } finally {

        }
    }

    protected String writeToFakeDB(String jsonString){
        try{
            File outputFile = new File(getServletContext().getRealPath("/") + "fake.db.json");
            FileWriter fstream = new FileWriter(outputFile,true);
            BufferedWriter fbw = new BufferedWriter(fstream);
            fbw.write("," + jsonString);  // comma needed for appending json doc
            fbw.newLine();
            fbw.close();
            return null;
        }catch (Exception e) {
            return e.getMessage();
        }
    }
}