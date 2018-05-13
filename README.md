
[![GitHub license](https://img.shields.io/github/license/0rtis/jsafe.svg?style=flat-square)](https://github.com/0rtis/jsafe/blob/master/LICENSE)
[![Build Status](https://img.shields.io/travis/0rtis/jsafe.svg?style=flat-square)](https://travis-ci.org/storj/core)
[![codecov](https://img.shields.io/codecov/c/github/0rtis/jsafe.svg?style=flat-square)](https://codecov.io/gh/0rtis/jsafe)


```
     _.---.._    
 .-"'        `;"--,
 J""--..__    | .'J
 |:`' - ..`""-.'  |             _  _____        __     
 |.        `":J   S	       | |/ ____|      / _|    		
 J:   ,-.    .|  J 	       | | (___   __ _| |_ ___ 		
 S    JS)|   ;S  | 	   _   | |\___ \ / _` |  _/ _ \		
 J.   `-'  .J   S '	  | |__| |____) | (_| | ||  __/		
  S:        :|  J/ 	   \____/|_____/ \__,_|_| \___|		
  |'._      :| /   		
   `c-.__'- .;S/ 
   
```

# JSafe - A lightweight, portable and cross-plateform safe

JSafe encrypt your file using [AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) encryption. It can be used as command line tool or with a _file explorer_ like interface.

**Decrypted data is never written on the drive** (except during file extraction requested by user).

![JSafe GUI demo](docs/img/demo-gui.gif?raw=true)

![JSafe CLI demo](docs/img/demo-cli.png?raw=true)




### Why JSafe ?
With the rise of online banking, cryptocurrencies and other digital transformation, it has become mandatory to backup sensitive files.
Those file need to be easily accessible, securely stored and encrypted. But lightweight, portable, cross platform vault software are surprisingly hard to come by. Password protected archive works fine but they let room for file leakage as there is no convenient way of exploring the vault without extracting the whole content.






### JSafe Protocol
JSafe is using a very simple protocol so encrypted files can be easily read by another program, as long as you have the encryption password.
Each datagram is preceded by its length stored as a 64 bits (8 bytes) integer (`long` in Java):

    length 0|datagram 0|length 1|datagram 1|length 3|...|datagram N
    
The first datagram `datagram 0` is the *header* and is **the only datagram not encrypted**. The *header* contains text entries specified by the user and various additional entries incuding a protocol explanation, the type of encoding and the IV of the encryption. The *header*'s data is stored in JSON format and can be seen by opening the safe file with a basic text editor.

The second datagram `datagram 1` is the *properties*. It contains encrypted text entries specified by the user.

The following datagrams (from 2 to N) are the encrypted files. They work by pair: `datagram i ` contains the metadata of the file as an encrypted JSON text and `datagram i+1` contains the bytes of the encrypted file.



### Donation
Like the project? Consider making a donation ! 
ETH & ERC-20: 0xaE247d13763395aD0B2BE574802B2E8B97074946
BTC: 18tJbEM2puwPBhTmbBkqKFzRdpwoq4Ja2a
BCH: 16b8T1LB3ViBUfePCMuRfZhUiZaV7tUxGn
LTC: Lgi89D1AmniNS8cxyQmXJhKm9SCXt8fQWC

### TODO
- [x] Command line
- [x] Wildcard path support
- [x] File explorer
- [x] Import with Drag & Drop
- [ ] Export with Drag & Drop
- [x] Text viewer
- [ ] Picture viewer
- [ ] Interactive shell

### Download
**This project is still under development and file loss may occur. Make sure to have a backup of your files !**

You can download the latest version of JSafe [here]()


### Install
JSafe is a portable application. No installation is required. Just run the JAR in a Command Line Interpreter or double click it to run the GUI. 


*JSafe is using the mighty tiny [picocli](https://github.com/remkop/picocli)*





