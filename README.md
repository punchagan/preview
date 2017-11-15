# Preview

Preview is a graphical preview tool for repositories. It is inspired by the NYT
internal tool described by @mbostock in [this
talk](https://www.youtube.com/watch?v=fThhbt23SGM).

## TODO

- Allow viewing the repository at any commit
- Support for multiple "projects", within a single repository?

## Developing

### Setup

When you first clone this repository, run:

```sh
lein duct setup
```

This will create files for local configuration, and prep your system
for the project.

### Configuration

To configure the project, set `:repository-root` in the
`dev/resources/local.edn` file, as below.

```clojure
{:duct.core/include ["dev"]
 :preview.handler/views {:repository-root "/path/to/repository/root"}}
```

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