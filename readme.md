Url Shortener
===================

Dev and Test spec:
Server **Apache Tomcat v9**
Language **Java, Servlet**

----------


Documents
-------------

**Shorten algorithm**

Use base62 hash an unique long integer signature representing an url.


**Local testing setting**

> **Note:**
>
> - Place source directory under ../tomcat/webapps
> - Open browser with localhost:port/test1/ to start with interface
> - Rest should be straight forward

----------


Route and APIs
-------------------

> **Note:**
>
> - **/v1 GET** To get all stored url and shorten representation with detail information such like expire time and owner name 
> - **/v1 POST** To submit the url that need shorten service, with a parameters string. Content type is **text/plain**.
>  parameters are accepted as **?o_url + "&" + is_private + "&" + expire_time + "&" + owner + "&" + level**
>  
> - **/r/{hash url} GET** To redirect to original url if the hash url is valid and not expired. If the hash url is invalid or expired, a json response will be returned. 
> ex    ```{"status":"Url already expired."}```

