

[![GitHub license](https://img.shields.io/github/license/0rtis/jsafebox.svg?style=flat-square)](https://github.com/0rtis/jsafebox/blob/master/LICENSE)
[![Build Status](https://img.shields.io/travis/0rtis/jsafebox.svg?style=flat-square)](https://travis-ci.org/0rtis/jsafebox)
[![codecov](https://img.shields.io/codecov/c/github/0rtis/jsafebox.svg?style=flat-square)](https://codecov.io/gh/0rtis/jsafebox)
[![Follow @Ortis95](https://img.shields.io/twitter/follow/Ortis95.svg?style=flat-square)](https://twitter.com/intent/follow?screen_name=Ortis95) 

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

JSafebox encrypt your files using [AES](https://en.wikipedia.org/wiki/Advanced_Encryption_Standard) encryption. It can be used as command line tool or with a _file explorer_ like interface.

**Decrypted data is never written on the drive** (except during file extraction requested by user).

![JSafebox GUI demo](docs/img/demo-gui.gif?raw=true)

![JSafebox CLI demo](docs/img/demo-cli.png?raw=true)




### Why JSafebox ?
With the rise of online banking, cryptocurrencies and other digital transformation, it has become mandatory to backup sensitive files.
Those file need to be easily accessible, securely stored and encrypted. But lightweight, portable, cross platform vault software are surprisingly hard to come by. Password protected archive works fine but they let room for file leakage since there is no convenient way of exploring the vault without extracting the whole content. Jsafebox was made to cover these shortfalls.






### JSafebox Protocol
JSafebox is using a very simple protocol so encrypted files can be easily read by another program, as long as you have the password. The encryption key is derived from the password using [PBKDF2](https://en.wikipedia.org/wiki/PBKDF2) hashing.

A JSafebox file contains a [SHA256](https://en.wikipedia.org/wiki/SHA-2) integrity hash followed by blocks:
	
	integrity hash | block 0 | block 1 | ... | block N

Each block is stored as followed: 

    IV | metadata length | metadata | data length | data
    
where `IV` is the [Initialization_vector](https://en.wikipedia.org/wiki/Initialization_vector) of the encryption (16 bytes), `metadata` is a JSON string and `length` are 64 bits (8 bytes) integer (`long` in Java).

The first block `block 0` is the *header* and is **the only block not encrypted** and therefore, **the only block without IV**. The *header* only have metadata (`data length` is 0) and contains text entries specified by the user and various additional entries including a protocol explanation, the type of encoding and the parameters of the encryption. The *header*'s metadata is stored as JSON string and can be seen by opening the safe file with a basic text editor.

The second block `block 1` is the *properties*. It is similar to the *header* except that it is encrypted and have an IV. The *properties* contains text entries specified by the user and stored in JSON.

The following blocks (from 2 to N) are the encrypted files. 



### TODO
- [x] Command line (with the mighty tiny [picocli](https://github.com/remkop/picocli) project)
- [x] Wildcard path support
- [x] File explorer
- [x] Import with Drag & Drop
- [x] Export ~~with Drag & Drop~~ (Risk of unintentional drag resulting in data leak)
- [x] Text viewer
- [x] Image viewer (zoom and drag)
- [x] Integrity check
- [ ] Interactive shell


### Download
**This project is still under development and file loss may occur. Make sure to have a backup of your files !**

You can download the latest version of JSafebox [here](https://github.com/0rtis/jsafebox/releases/latest)



### Install
JSafebox is a portable application. No installation is required. Just run the JAR in a Command Line Interpreter or double click it to run the GUI. 


### Donation
Like the project ? Consider making a donation 

ETH & ERC-20: _0xaE247d13763395aD0B2BE574802B2E8B97074946_

BTC: _18tJbEM2puwPBhTmbBkqKFzRdpwoq4Ja2a_

BCH: _16b8T1LB3ViBUfePCMuRfZhUiZaV7tUxGn_

LTC: _Lgi89D1AmniNS8cxyQmXJhKm9SCXt8fQWC_

MCM: _010000000000003072746973_



