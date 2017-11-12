/*
  @params:

    id : (DB generated sequence ID) in traditional SQL, not useable in noSQL
    o_url : URL value
    own : user name
    create time : useful as part of the salt

  note: Two type of urls:
        1. url that generated anonmyously: in noSQL, this is hard becuase there is no definite "unique" thing to hash.
           But in triditonal DB, a index number with base64 encoder can be used to hash. To prevent predicbility, a time gap
           between current request and previous(global) request can be use as a good salt, since in real practice only auto
           increased ID will be used, there is no concurrency issue.
        2. url that generate by a login user: user_id, username, email are unique, and in real world, 1 user
           can only request 1 short url at a time. Therefore, unique identifier + request time togather is very unique for
           prevent concurrency issue.

*/

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.*;
import java.sql.*;
import java.util.logging.Logger;


public class Hash{
    private String hash_url;
    private String owner;
    private String hash_level;
    private String o_url;
    private String create_time;
    private long id;
    private List<String> jsonStore;
    private static final Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName() );

    public Hash(String owner, String hash_level, String o_url, String create_time, long id, List<String> jsonStore){
        this.o_url =o_url;
        this.owner = owner;
        this.create_time = create_time;
        this.hash_level = hash_level;
        this.id = id;
        this.jsonStore = jsonStore;
    }

    public String getHashUrl(){
        Timestamp now = new Timestamp(System.currentTimeMillis());
        int level = Integer.parseInt(hash_level);
        // general url, visitor request
        if (owner.length()==0){
            Timestamp ts_now = Timestamp.valueOf( create_time );
            String salt = now.toString() + ts_now.toString();
            long baseID = id + salt.hashCode();
            try {
                Base62Converter c = new Base62Converter(baseID, o_url);
                c.setHashLevel(level);
                return c.convertTo62Base();
            }catch (NumberFormatException e){
                e.printStackTrace();
                return "";
            }
        }else{ // login user request
            long salt = Math.abs(create_time.hashCode() + owner.hashCode() + now.hashCode());
            long baseID = id + salt;
            LOGGER.info("\nUUID: "+baseID);
            try {
                Base62Converter c = new Base62Converter(baseID, o_url);
                c.setHashLevel(level);
                return c.convertTo62Base();
            }catch (NumberFormatException e){
                e.printStackTrace();
                return "";
            }
        }


    }
}