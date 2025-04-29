#!/bin/bash

set -e

echo "Waiting for SQL Server to be ready..."
until /opt/mssql-tools/bin/sqlcmd -S sqlserverdb -U SA -P 'SqlS3rv3r23' -Q "SELECT 1" &>/dev/null
do
  sleep 1
done

/opt/mssql-tools/bin/sqlcmd -S sqlserverdb -U SA -P 'SqlS3rv3r23' -i /sql/setup.sql

