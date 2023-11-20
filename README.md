## Accessing your API

We've installed `curl` in the container running your application, so you can make requests to your API directly from the Shell. For instance, you can run `curl 127.0.0.1:3000/transactions` to see your server's output. 

## Connecting to Database

The database is a PostgreSQL database. The user and password is configured in the `application.yml` file. To connect to the local instance, type: `psql -U postgres -W postgresql://localhost:5432/postgres`.

## Shell

A shell is provided to you so you can inspect your container in more detail. The shell can be used to run maven commands with `mvn`## Accessing your API

We've installed `curl` in the container running your application, so you can make requests to your API directly from the Shell. For instance, you can run `curl 127.0.0.1:3000/transactions` to see your server's output. 

## Connecting to Database

The database is a PostgreSQL database. The user and password is configured in the `application.yml` file. To connect to the local instance, type: `psql -U postgres -W postgresql://localhost:5432/postgres`.

## Shell

A shell is provided to you so you can inspect your container in more detail. The shell can be used to run maven commands with `mvn`