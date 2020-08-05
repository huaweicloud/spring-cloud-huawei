module demo

require (
	github.com/emicklei/go-restful v2.12.0+incompatible
	github.com/go-chassis/go-chassis v1.8.2-0.20200803103444-d7b1d63e60bc
	github.com/go-chassis/go-chassis-extension/tracing/zipkin v0.0.0-20200803124338-47eb21ed82a7
	github.com/go-mesh/openlogging v1.0.1
	github.com/openzipkin-contrib/zipkin-go-opentracing v0.3.5
)

replace github.com/openzipkin-contrib/zipkin-go-opentracing v0.3.5 => github.com/go-chassis/zipkin-go-opentracing v0.3.5-0.20190321072447-42cf74fc2a92

go 1.13
