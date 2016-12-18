# JavaHttpServer
HTTP/1.0 server in Java

###Server
* open a listen socket that could handle HTTP/1.0 requests.

* compatible clients (e.g. a web browser such as Mozilla Firefox)can connect to in order to request pages and files.

* HTTP/1.0 is defined in RFC 1945, available from http://tools.ietf.org/pdf/rfc1945.pdf

* arguments: `<port number> <root name> <-r | -R> <log path>`
    * root name: the folder to host the file e.g. `./root`
    * -r: turn on the log
    * -R: turn on the log and reset the log file
    
* Log is implemented in observer pattern. The new log could be easily integrated.

###Client
* Check the availability and last-modified data of each link in a page.
* Retrieve each working page which has a last-modified data within the last 6 months. 
* For each retrieved page, out put all the headings of that page.
