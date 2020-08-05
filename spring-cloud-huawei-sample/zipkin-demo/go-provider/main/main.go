package main

import (
	"demo/controller"
	"github.com/go-chassis/go-chassis"
	_ "github.com/go-chassis/go-chassis-extension/tracing/zipkin"
	"github.com/go-chassis/go-chassis/core/server"
	"github.com/go-mesh/openlogging"
)

func main() {
	chassis.RegisterSchema("rest", &controller.MainRouter{}, server.WithSchemaID("RestHelloService"))
	if err := chassis.Init(); err != nil {
		openlogging.Error("Init failed." + err.Error())
		return
	}
	chassis.Run()
}
