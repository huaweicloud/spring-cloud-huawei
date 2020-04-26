package model

type Info struct {
	Var1 string    `json:"var1,omitempty" yaml:"var1,omitempty"`
	Var2 int       `json:"var2,omitempty" yaml:"var2,omitempty"`
	Var3 MinorInfo `json:"var3,omitempty" yaml:"var3,omitempty"`
}

type MinorInfo struct {
	Info  string `json:"info,omitempty" yaml:"info,omitempty"`
	Dummy bool   `json:"dummy,omitempty" yaml:"dummy,omitempty"`
}
