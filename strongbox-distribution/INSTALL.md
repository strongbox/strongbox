sudo su -
groupadd strongbox
useradd -d /usr/local/strongbox -g strongbox strongbox
chown -R strongbox:strongbox /usr/local/strongbox/

tar -zxf strongbox-distribution-1.0-SNAPSHOT.tar.gz -C /usr/local/strongbox --strip-components=1
ln -s /usr/local/strongbox/bin/wrapper-linux-x86-64 /usr/local/strongbox/bin/wrapper
