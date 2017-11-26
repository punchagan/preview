# Preview

Preview is a graphical preview tool for git repositories. It is inspired by the
NYT internal tool described by [@mbostock](https://github.com/mbostock) in [this
talk](https://www.youtube.com/watch?v=fThhbt23SGM).

## Developing

### Setup

When you first clone this repository, run:

```sh
lein duct setup
```

This will create files for local configuration, and prep your system
for the project.

### Configuration

To configure the project, set the following config in the
`dev/resources/local.edn` file:

```clojure
{:duct.core/include ["dev"]
 :preview.handler/views {:preview-root "/path/to/repo/root"
                         :preview-gh-user "UserName"}}
```

- `:preview-root` is the directory used by Preview to save the cloned repos,
  screenshots and other data.

- `:preview-gh-user` is the GitHub username of the user to follow using Preview.

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
modified files and reset the server.

```clojure
dev=> (reset)
:reloading (...)
:resumed
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

Copyright © 2017 Puneeth Chaganti

LICENSE AGPL-3.0
