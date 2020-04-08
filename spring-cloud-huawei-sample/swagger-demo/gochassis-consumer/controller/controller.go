package controller

import (
	"context"
	"demo/controller/model"
	"encoding/json"
	"github.com/go-chassis/go-chassis/client/rest"
	"github.com/go-chassis/go-chassis/core"
	"github.com/go-chassis/go-chassis/core/lager"
	"github.com/go-chassis/go-chassis/pkg/util/httputil"
	rf "github.com/go-chassis/go-chassis/server/restful"
	"net/http"
)

type MainRouter struct {
}

func (r *MainRouter) HelloSpringCloud(b *rf.Context) {
	req, err := rest.NewRequest("GET", "http://swagger-consumer/consumer/invoke", nil)
	if err != nil {
		lager.Logger.Error("new request failed.")
		return
	}
	resp, _ := core.NewRestInvoker().ContextDo(context.TODO(), req)
	result := httputil.ReadBody(resp)
	b.Write(result)
}

func (r *MainRouter) Hello(b *rf.Context) {
	b.Write([]byte("hello"))
}

func (r *MainRouter) LongCall(b *rf.Context) {
	info := new(model.Info)
	err := json.NewDecoder(b.ReadRequest().Body).Decode(info)
	if err != nil {
		lager.Logger.Error("Decode failed.")
		return
	}
	body, err := json.Marshal(info)
	req, err := rest.NewRequest("POST", "http://swagger-consumer/consumer/callBack", body)
	if err != nil {
		lager.Logger.Error("new request failed.")
		return
	}
	req2, err := rest.NewRequest("POST", "http://swagger-provider/callBack", body)
	if err != nil {
		lager.Logger.Error("new request failed.")
		return
	}
	resp, _ := core.NewRestInvoker().ContextDo(context.TODO(), req)
	resp2, _ := core.NewRestInvoker().ContextDo(context.TODO(), req2)
	result := httputil.ReadBody(resp)
	result2 := httputil.ReadBody(resp2)
	b.Write(result)
	b.Write(result2)
}

func (r *MainRouter) CallBack(b *rf.Context) {
	info := new(model.Info)
	err := json.NewDecoder(b.ReadRequest().Body).Decode(info)
	if err != nil {
		lager.Logger.Error("Decode failed.")
		return
	}
	b.Write([]byte("go chassis : " + info.Var3.Info))
}

func (s *MainRouter) URLPatterns() []rf.Route {
	return []rf.Route{
		{Method: http.MethodGet, Path: "/hello-java-chassis", ResourceFunc: s.HelloSpringCloud,
			Returns: []*rf.Returns{{Code: 200}}},
		{Method: http.MethodGet, Path: "/hello", ResourceFunc: s.Hello,
			Returns: []*rf.Returns{{Code: 200}}},
		{Method: http.MethodPost, Path: "/longCall", ResourceFunc: s.LongCall,
			Returns: []*rf.Returns{{Code: 200}}},
		{Method: http.MethodPost, Path: "/callBack", ResourceFunc: s.CallBack,
			Returns: []*rf.Returns{{Code: 200}}},
	}
}
