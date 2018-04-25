# open-vpn-console

Operation console for OpenVPN on AWS EC2.  
This repository is under development.

## Usage

You have to set AWS credentials as environmental variables.  
And this cred nedds to be able to operate on EC2.

```
export AWS_ACCESS_KEY=xxxxxxxxxxxxx
export AWS_SECRET_KEY=xxxxxxxxxxxxx
export AWS_ENDPOINT=ap-northeast-1
export LOGIN_EMAIL=xxxx@example.com
export LOGIN_PASSWORD=xxxxxxxx
```

and you have to add following files.

resources/openvpn/ca.crt
resources/openvpn/client1.crt
resources/openvpn/client1.key
resources/openvpn/ta.key

## Developing

### Setup

When you first clone this repository, run:

```sh
lein duct setup
```

This will create files for local configuration, and prep your system
for the project.

Next connect the repository to the [Heroku][] app:

```sh
heroku git:remote -a FIXME
```

[heroku]: https://www.heroku.com/

### Environment

To begin developing, start with a REPL.

```sh
lein repl
```

Then load the development environment.

```clojure
user=> (dev)
:loaded
```

Run `go` to prep and initiate the system.

```clojure
dev=> (go)
:duct.server.http.jetty/starting-server {:port 3000}
:initiated
```

By default this creates a web server at <http://localhost:3000>.

When you make changes to your source files, use `reset` to reload any
modified files and reset the server. Changes to CSS or ClojureScript
files will be hot-loaded into the browser.

```clojure
dev=> (reset)
:reloading (...)
:resumed
```

If you want to access a ClojureScript REPL, make sure that the site is loaded
in a browser and run:

```clojure
dev=> (cljs-repl)
Waiting for browser connection... Connected.
To quit, type: :cljs/quit
nil
cljs.user=>
```

### Testing

Testing is fastest through the REPL, as you avoid environment startup
time.

```clojure
dev=> (test)
...
```

But you can also run tests through Leiningen.

```sh
lein test
```

## Legal

Copyright © 2018 Kazuki Tsutsumi
