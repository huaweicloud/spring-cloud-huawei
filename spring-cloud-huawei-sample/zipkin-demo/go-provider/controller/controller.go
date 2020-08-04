package controller

import (
	rf "github.com/go-chassis/go-chassis/server/restful"
	"net/http"
)

type MainRouter struct {
}

func (r *MainRouter) Hello(b *rf.Context) {
	b.Write([]byte("hello"))
}

func (s *MainRouter) URLPatterns() []rf.Route {
	return []rf.Route{
		{Method: http.MethodGet, Path: "/hello", ResourceFunc: s.Hello,
			Returns: []*rf.Returns{{Code: 200}}},
	}
}
