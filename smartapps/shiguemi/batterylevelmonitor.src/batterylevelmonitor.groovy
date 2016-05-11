/**
 *  BatteryLevelMonitor
 *
 *  Copyright 2016 Leonardo Shiguemi Dinnouti
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "BatteryLevelMonitor",
    namespace: "Shiguemi",
    author: "Leonardo Shiguemi Dinnouti",
    description: "Clone of battery level from smartthins article",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select Battery-powered devices") {
		input "bats", "capability.battery", multiple: true
        input "thresh", "number", title: "If the battery goes below this level, " +
        	"send me a push notification"          
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	setup()
}

def setup() {
	def params = [ 
    	uri: "http://smartthings-1307.appspot.com/demo",
        path: ""
    ]
    
    try {
    	httpGet(params) { resp ->
        	resp.headers.each {
            	log.debug "${it.name}: ${it.value}"
                if (${it.name} == 'serverUpdateValue') {
                	state.serverUpdateValue = ${it.value}
                } else if (${it.name} == 'method') {
                	state.method = ${it.value}
                } else if (${it.name} == 'destIP') {
                	state.destIP = ${it.value}
                } else if (${it.name} == 'data') {
                	state.data = ${it.value}
                }
            }
//            def jsonSlurper = new JsonSlurper()
//            def jsonString = resp.data.text
//            def configJson = jsonSlurper.parseText(jsonString)
//            state.serverUpdateValue = configJson['serverUpdateValue']
//            state.method = configJson['method']
//            state.destIP = configJson['destIP']
//            state.data = configJson['data']
        }
    } catch (e) {
    	log.error "something went wrong: $e"
    }
    
    bats.each { b ->
    	//subscribe(b, state.serverUpdateValue, handler)
    	subscribe(b, "contact", handler)
    }
}

def handler(evt) {
    if (evt.device?.currentBattery < thresh) {
    	sendPush("Battery low for device ${evt.deviceId}")
    }

	try {
//    	"${state.method}"("${state.destIP}", "contact=${evt.value}") { resp ->
//    	httpPost("${state.destIP}", "contact=${evt.value}") { resp ->
//    	httpPost("${state.destIP}", "contact=${evt.value}") { resp ->
        httpPost("http://smartthings-1307.appspot.com/demo", "contact=${evt.value}") { resp ->
        	log.debug "response data: ${resp.data}"
            log.debug "response contentType: ${resp.contentType}"
        }
    } catch (e) {
    	log.error "something went wrong: $e"
    }
    
}
