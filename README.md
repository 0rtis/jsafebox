
[![GitHub license](https://img.shields.io/github/license/0rtis/jsafebox.svg?style=flat-square)](https://github.com/0rtis/jsafebox/blob/master/LICENSE)
[![Build Status](https://img.shields.io/travis/0rtis/jsafebox.svg?style=flat-square)](https://travis-ci.org/0rtis/jsafebox)
[![codecov](https://img.shields.io/codecov/c/github/0rtis/jsafebox.svg?style=flat-square)](https://codecov.io/gh/0rtis/jsafebox)


```
     _.---.._    
 .-"'        `;"--,
 J""--..__    | .'J
 |:`' - ..`""-.'  |	         _  _____        __     _               
 |.        `":J   S	        | |/ ____|      / _|   | |              		
 J:   ,-.    .|  J 	        | | (___   __ _| |_ ___| |__   _____  __		
 S   (JSB)   ;S  | 	    _   | |\___ \ / _` |  _/ _ \ '_ \ / _ \ \/ /		
 J.   `-'  .J   S '	   | |__| |____) | (_| | ||  __/ |_) | (_) >  < 		
  S:        :|  J/ 	    \____/|_____/ \__,_|_| \___|_.__/ \___/_/\_\		
  |'._      :| /   		
   `c-.__'- .;S/ 
     
```

# JSafebox - A lightweight, portable and cross-platform vault

JSafebox encrypt your file using [AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) encryption. It can be used as command line tool or with a _file explorer_ like interface.

**Decrypted data is never written on the drive** (except during file extraction requested by user).

![JSafebox GUI demo](docs/img/demo-gui.gif?raw=true)

![JSafebox CLI demo](docs/img/demo-cli.png?raw=true)




### Why JSafebox ?
With the rise of online banking, cryptocurrencies and other digital transformation, it has become mandatory to backup sensitive files.
Those file need to be easily accessible, securely stored and encrypted. But lightweight, portable, cross platform vault software are surprisingly hard to come by. Password protected archive works fine but they let room for file leakage as there is no convenient way of exploring the vault without extracting the whole content. Jsafebox was made to cover these shortfalls.






### JSafebox Protocol
JSafebox is using a very simple protocol so encrypted files can be easily read by another program, as long as you have the encryption password.
Each datagram is preceded by its length stored as a 64 bits (8 bytes) integer (`long` in Java):

    length 0|datagram 0|length 1|datagram 1|length 3|...|datagram N
    
The first datagram `datagram 0` is the *header* and is **the only datagram not encrypted**. The *header* contains text entries specified by the user and various additional entries incuding a protocol explanation, the type of encoding and the IV of the encryption. The *header*'s data is stored in JSON format and can be seen by opening the safe file with a basic text editor.

The second datagram `datagram 1` is the *properties*. It contains encrypted text entries specified by the user.

The following datagrams (from 2 to N) are the encrypted files. They work by pair: `datagram i ` contains the metadata of the file as an encrypted JSON text and `datagram i+1` contains the bytes of the encrypted file.



### TODO
- [x] Command line
- [x] Wildcard path support
- [x] File explorer
- [x] Import with Drag & Drop
- [ ] Export with Drag & Drop
- [x] Text viewer
- [x] Image viewer (zoom and drag)
- [ ] Interactive shell


### Download
**This project is still under development and file loss may occur. Make sure to have a backup of your files !**

You can download the latest version of JSafebox [here](https://github.com/0rtis/jsafebox/releases/latest)

*JSafebox is using the mighty tiny [picocli](https://github.com/remkop/picocli)*


### Install
JSafebox is a portable application. No installation is required. Just run the JAR in a Command Line Interpreter or double click it to run the GUI. 


### Donation
Like the project ? Consider making a donation :) 

ETH & ERC-20: _0xaE247d13763395aD0B2BE574802B2E8B97074946_

BTC: _18tJbEM2puwPBhTmbBkqKFzRdpwoq4Ja2a_

BCH: _16b8T1LB3ViBUfePCMuRfZhUiZaV7tUxGn_

LTC: _Lgi89D1AmniNS8cxyQmXJhKm9SCXt8fQWC_

