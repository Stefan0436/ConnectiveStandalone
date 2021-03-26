# NOTICE
The github repository mirrors to https://aerialworks.ddns.net/ASF/ConnectiveStandalone.git, please use that repo instead of github if you wish to contribute.

<br />

# ConnectiveHTTP Standalone Server
A standalone program for the ConnectiveHTTP server.

<br />
<br />

## Building
First you will need java and gradle, after that, building is very simple:<br/>


#### Linux commands:

```bash
# On linux, we can update the RaTs! Binaries to make sure we compile for the latest version of Connective
chmod +x updaterats.sh
./updaterats.sh

chmod +x gradlew connective
./gradlew installation
```

#### Windows commands:
```batch
gradlew.bat installation
```

... Both linux and windows output in the `build/Installations` directory.

<br />

## Running the server (you will need to have build the server)
To run ConnectiveHTTP, java needs to be installed. <br />
After which, you just need to run the right script for your operating system:

#### Linux:
```bash
cd build/Installations
./connective
```

#### Windows:
```batch
cd build\Installations
connective.bat
```

<br />

## Server configuration
Read [CONFIGURING.md](CONFIGURING.md) for more info.
