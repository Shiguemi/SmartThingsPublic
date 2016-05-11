/**
 *  Web Services Tutorial
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
    name: "Web Services Tutorial",
    namespace: "smartthings",
    author: "SmartThings",
    description: "web services tutorial",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "web services tutorial ", displayLink: ""])


preferences {
  section ("Allow external service to control these things...") {
    input "switches", "capability.switch", multiple: true, required: false
    input "contact", "capability.contactSensor", required: true, title: "Select a contact sensor."
  }
}

mappings {
  path("/switches") {
    action: [
      GET: "listSwitches"
    ]
  }
  path("/contacts") {
  	action: [
    	GET: "listContacts"
    ]
  }
  path("/switches/:command") {
    action: [
      PUT: "updateSwitches"
    ]
  }
}

preferences(oauthPage: "deviceAuthorization") {
    // deviceAuthorization page is simply the devices to authorize
    page(name: "deviceAuthorization", title: "", nextPage: "instructionPage",
         install: false, uninstall: true) {
        section("Select Devices to Authorize") {
            input "switches", "capability.switch", required: false, title: "Switches:"
            input "contact", "capability.contactSensor", title: "Select a contact sensor."
        }

    }

    page(name: "instructionPage", title: "Device Discovery", install: true) {
        section() {
            paragraph "Testing web services. I don't know what to explain here :-("
        }
    }
}

// returns a list like
// [[name: "kitchen lamp", value: "off"], [name: "bathroom", value: "on"]]
def listSwitches() {

    def resp = []
    switches.each {
        resp << [name: it.displayName, value: it.currentValue("switch")]
    }
    return resp
}

def listContacts() {
	log.debug "listContacts() called"
	def resp = [[name: "contact",  value: "debug 2016-04-14 17:25"]]
    resp << [ name: "contact", value: state.contactStatus]
    log.debug "listContacts: ${resp}"
    return resp
}

void updateSwitches() {
    // use the built-in request object to get the command parameter
    def command = params.command

    if (command) {

        // check that the switch supports the specified command
        // If not, return an error using httpError, providing a HTTP status code.
        switches.each {
            if (!it.hasCommand(command)) {
                httpError(501, "$command is not a valid command for all switches specified")
            } 
        }
        
        // all switches have the comand
        // execute the command on all switches
        // (note we can do this on the array - the command will be invoked on every element
        switches."$command"()
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
	state.contactStatus = "undefined"
	subscribe(contact, "contact", contactHandler)
    log.debug "initialize() called"
    //sendPush("Hello World Tutorial running")
}

// TODO: implement event handlers

def contactHandler(evt) {
	if ("open" == evt.value) {	
        log.debug("contact is in open state")
        //sendPush("contact is in open state")
    }
    if ("closed" == evt.value) {
    	log.debug("contact is in closed state")
        //sendPush("contact is in closed state")
    }
    state.contactStatus = evt.value
}