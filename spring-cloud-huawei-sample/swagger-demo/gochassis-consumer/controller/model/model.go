/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
