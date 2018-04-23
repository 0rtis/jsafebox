# JSafe - A standalone, cross-plateform virutal safe

JSafe encrypt your file using AES encryption. It can be used as command line tool or with a _file explorer_ like interface.

*picture of file explorer*
*picture of command line and example*








**Decrypted data is never wrote on disk** (except during file extraction requested by user).


### Encryption protocol


### TODO
- [x] Command line
- [x] Wildcard path
- [ ] File explorer
- [ ] Text viewer
- [ ] Picture viewer

#### Why JSafe ?

- It is hard to find a standalone and cross platform vault software
- Password protected archive works fine but they let room for file leakage as there is no convenient way of exploring the vault
  unless extracting the whole content
- Need for both file explorer GUI and command line capabilities



**This project is still under developement. Make sure to have a backup of your file somewhere else if you plan to use it**

*JSafe is using the mighty [picocli](https://github.com/remkop/picocli)*
