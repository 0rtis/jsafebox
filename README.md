

```
     _.---.._    
 .-"'        `;"--,
 L""--..__    | .'J
 |:`' - ..`""-.'  |             _  _____        __     
 |.        `":J   S	       | |/ ____|      / _|    		
 J:   ,-.    .|  J 	       | | (___   __ _| |_ ___ 		
 S    JS)|   ;S  | 	   _   | |\___ \ / _` |  _/ _ \		
 J.   `-'  .J   S '	  | |__| |____) | (_| | ||  __/		
  S:        :|  J/ 	   \____/|_____/ \__,_|_| \___|		
  |'._      :| /   		
   `c-.__'- .;S/ 
   
```

# JSafe - A standalone, cross-plateform virtual safe

JSafe encrypt your file using [AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) encryption. It can be used as command line tool or with a _file explorer_ like interface.

**Decrypted data is never wrote on disk** (except during file extraction requested by user).

*picture of file explorer*

*picture of command line and example*











### Encryption protocol
JSafe is using a very simple protocol so the safe file can be easily descrypted by another program, as long as you have the encryption password.
Each datagram is preceded by its length stored as a 64 bits (8 bytes) integer (`long` in Java):

    length 0|datagram 0|length 1|datagram 1|length 3|...|datagram N
    
The first datagram `datagram 0` is the *header* and is **the only datagram not encrypted**. The *header* contains text entries specified by the user and various additional entries incuding a protocol explanation, the type of encoding and the IV of the encryption. The *header*'s data is stored in JSON format can seen by opening the safe file with a basic text editor.

This second datagram `datagram 1` is the *properties*. It contains encrypted text entries specified by the user.

The following datagrams (from 2 to N) are the encrypted files. They worked by pair: `datagram i ` contains the metadata of the file an encrypted JSON text and `datagram i+1` contains the bytes of the encrypted file.


### Why JSafe ?
With the avent of online banking, cryptocurrencies and other digital transformation, it has become mandatory to backup sensitive files.
Those file need to be easily accesible, securely stored and encrypted. But lightweight, standalone, cross platform vault software are surprisingly hard to come by. Password protected archive works fine but they let room for file leakage as there is no convenient way of exploring the vault unless extracting the whole content.



### TODO
- [x] Command line
- [x] Wildcard path
- [ ] File explorer
- [ ] Text viewer
- [ ] Picture viewer



**This project is still under developement. Make sure to have a backup of your files somewhere else if you plan to use it !**

*JSafe is using the mighty [picocli](https://github.com/remkop/picocli)*
