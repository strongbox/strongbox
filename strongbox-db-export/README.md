Module to export strongbox orient database and create database snapshot.

See also:
* http://orientdb.com/docs/3.0.x/console/Console-Command-Export.html
* http://orientdb.com/docs/3.0.x/admin/Export-to-and-Import-from-JSON.html

Usage example:
Main class: org.carlspring.strongbox.ext.OrientDbExportMain
Program arguments: --url=remote:localhost:2025/strongbox --username=admin --password=password --fileName=/tmp/strongbox-db-exported-snapshot --includeRecordsClusters=DATABASECHANGELOG,DATABASECHANGELOG_1,DATABASECHANGELOG_2,DATABASECHANGELOG_3,OROLE,OUSER