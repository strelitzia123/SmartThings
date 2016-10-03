/**
 *  Smappee Device Type modified from a Filtrete 3M-50 WiFi Thermostat
 *
 *  For original radio thermostat device type information, please visit:
 *
 *  <https://github.com/statusbits/smartthings/tree/master/RadioThermostat/>
 *
 *  --------------------------------------------------------------------------
 *
 *  Copyright (c) 2015 David Tucker
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  --------------------------------------------------------------------------
 *
 *  Version 1.0.1 (04/11/2015)
 */

import groovy.json.JsonSlurper

preferences {
    input("confIpAddr", "string", title:"Smappee IP Address",
        required:true, displayDuringSetup: true)
    input("confTcpPort", "number", title:"Smappee TCP Port",
        required:true, displayDuringSetup:true)
}

metadata {
    definition (name:"Smappee Energy Monitor2", namespace:"strelitzia123", author:"David Tucker") {

        capability "Sensor"
        capability "Refresh"
        capability "Polling"
        capability "Energy Meter"
        capability "Power Meter"
        capability "Actuator"

       // Custom attributes
        attribute "solar", "number"
        attribute "load", "number"
        attribute "powerMode", "string"
        attribute "logonTime", "string"
	attribute "power_details", "string"
	attribute "power_str", "string"
	attribute "solar_str", "string"
	attribute "load_str", "string"
        

        // Custom commands
        command "logOnSmappee"
    }

    tiles (scale :2) {
   		 // this tile is used for display in device list (to get correct colorization)
		valueTile(
			"power",
			"device.power") {
				state("power",
					label: '${currentValue}W',
					unit: "W",
					icon: "https://raw.githubusercontent.com/ahndee/Envoy-ST/master/devicetypes/aamann/enlighten-envoy-local.src/Solar.png",
					backgroundColors: [
						[value: 0, color: "#bc2323"],
						[value: 3000, color: "#1e9cbb"],
						[value: 6000, color: "#90d2a7"]
					])
		}
	    
            multiAttributeTile(name:"PowerMulti", type:"generic", width:6, height:4) {
            tileAttribute("device.power", key: "PRIMARY_CONTROL") {
                attributeState "default", label:'${currentValue}W', backgroundColors: [ 
                     [value: -1, color: "#00FF00"], 
                     [value: 0, color: "#FF0000"]
 		            ] 
            }
            tileAttribute ("powerMode", key: "SECONDARY_CONTROL") {
  				attributeState "Import", label: '${currentValue}'
  				attributeState "Export", label:'${currentValue}'
			}
            
        }
	    // the following tiles are used for display in the device handler
		multiAttributeTile(
			name:"SolarMulti",
			type:"generic",
			width:6,
			height:4) {
				tileAttribute("device.power", key: "PRIMARY_CONTROL") {
					attributeState("power",
						label: '${currentValue}W',
						icon: "https://raw.githubusercontent.com/ahndee/Envoy-ST/master/devicetypes/aamann/enlighten-envoy-local.src/Solar-2.png",
						unit: "W",
						backgroundColors: [
							[value: 0, color: "#bc2323"],
							[value: 3000, color: "#1e9cbb"],
							[value: 6000, color: "#90d2a7"]
						])
			}
			tileAttribute("device.power_details", key: "SECONDARY_CONTROL") {
				attributeState("power_details",
					label: '${currentValue}')
			}
		}
    /*
        valueTile("power", "device.power", width: 4, height: 4) {
			state "power", label:'${currentValue}W\nImport Export', unit:"W", backgroundColors: [ 
                     [value: -2000, color: "#00FF00"], 
                     [value: -1000, color: "#66FF99"], 
                     [value: -500, color: "#CCFF99"], 
                     [value: 0, color: "#808080"], 
 		            [value: 500, color: "#FFFF99"], 
                     [value: 1000, color: "#FFFF00"], 
                     [value: 2000, color: "#FF9933"],
                     [value: 3000, color: "#FF0000"]
     	            ] 

            
		}
        
        */
        
        standardTile("powerMode", "device.powerMode", decoration: "flat", width: 2, height: 2) {
			state "Import", icon:"st.Weather.weather8", label: "Import", backgroundColor: "#00FF00"
			state "Export", icon:"st.custom.wu1.sunny", label: "Export", backgroundColor: "#FF0000"
        }
        
        valueTile("load", "device.load", width: 2, height: 2) {
			state "load", label: '${currentValue}W\nLoad'
            
		}
        
        valueTile("solar", "device.solar", decoration: "ring", width:2, height:2) {
            state "default", label:'${currentValue} W\nSolar', backgroundColor: "#99C031"
        }
        /*
        valueTile("solar", "device.solar", width: 2, height: 2) {
			state "solar", label:'${currentValue}W\nSolar', unit:"W", backgroundColors: [ 
                    [value: 0, color: "#0D0D0D"], 
                    [value: 5, color: "##FF9900"], 
 		            [value: 250, color: "#33CC33"] 
                    ]     
		}
        */
        standardTile("refresh", "device.thermostatMode", inactiveLabel:false, decoration:"flat") {
            state "default", icon:"st.secondary.refresh", action:"refresh.refresh"
        }
		standardTile("logon", "device.thermostatMode", inactiveLabel:false, decoration:"flat") {
            state "default", action:"logOnSmappee", label: "LogOn"
        }
	htmlTile(name:"graphHTML",
			action: "getGraphHTML",
			refreshInterval: 1,
			width: 6,
			height: 4,
			whitelist: ["www.gstatic.com"])

        main "power"

        details(["PowerMulti", "SolarMulti", "solar", "powerMode", "load", "refresh", "logon"])
         
    }

    simulator {
        
    }
}

mappings {
	path("/getGraphHTML") {action: [GET: "getGraphHTML"]}
}




def updated() {
	//log.debug "In Updated()"
    log.info "Smappee LAN ${textVersion()}. ${textCopyright()}"
	LOG("$device.displayName updated with settings: ${settings.inspect()}")
	//log.debug state.hostaddress
    //log.debug settings.confIpAddr
    //log.debug settings.confTcpPort
    state.hostAddress = "${settings.confIpAddr}:${settings.confTcpPort}"
    //log.debug state.hostaddress
    //log.debug state.dni
    state.dni = createDNI(settings.confIpAddr, settings.confTcpPort)
    //log.debug state.dni

    //STATE()
}

def parse(String message) {
	//log.debug "In parse()"
    //LOG("parse(${message})")
	
    def t0 = new Date(now() - state.logonTime).format("h:mm:ss", location.timeZone)
    LOG("Time Since Logon Success: ${t0}")
    def msg = stringToMap(message)

    if (msg.headers) {
        // parse HTTP response headers
        def headers = new String(msg.headers.decodeBase64())
        def parsedHeaders = parseHttpHeaders(headers)
        //LOG("parsedHeaders: ${parsedHeaders}")
        if (parsedHeaders.status != 200) {
            log.error "Server error: ${parsedHeaders.reason}"
            return null
        }
        
        // parse HTTP response body
        if (!msg.body) {
            log.error "HTTP response has no body"
            return null
        }
		
        //log.debug msg.body
        
        def body = new String(msg.body.decodeBase64())
        
        //LOG("body: ${body}")
        
        def slurper = new JsonSlurper()
        def result = slurper.parseText(body)
		LOG("result(${result})")
        //log.debug result
        
        try {
    	
        log.debug result.containsKey("success")
        if (result.containsKey("success")) {
        	LOG("Success(${result.success})")
        	state.logonTime = now()
            LOG("logonTime(${state.logonTime})")
        	
            return null
        	}
		} catch (e) {
    	log.debug "caught exception trying to find 'success' in parsed body, this means the body is not the logon message"
		}
        
        //log.debug result.contains("success")
        /*
        if (result.contains("success")) {
           	LOG("Success(${result.success})")
        	state.logonTime = now()
            LOG("logonTime(${state.logonTime})")
        	
            return null
        }
        */
        return parseTstatData(result)
    } else if (msg.containsKey("simulator")) {
        // simulator input
        return parseTstatData(msg)
    }

    return null
}


// polling.poll 
def poll() {
    LOG("poll()")
    return refresh()
}

// refresh.refresh
def refresh() {
    LOG("refresh()")
    STATE()
    
    logOnSmappee()
    
    //apiPost("/gateway/apipublic/logon","admin")              
    //log.debug "logon completed"
    
    // next line used to get large data result, too much to handle
    //return apiGet("/gateway/apipublic/reportInstantaneousValues")
    
    // next line testing to get smaller set of data
    return apiPost("/gateway/apipublic/instantaneous","loadInstantaneous")
    
}

// Creates Device Network ID in 'AAAAAAAA:PPPP' format

private String createDNI(ipaddr, port) { 
    LOG("createDNI(${ipaddr}, ${port})")

    def hexIp = ipaddr.tokenize('.').collect {
        String.format('%02X', it.toInteger())
    }.join()

    def hexPort = String.format('%04X', port.toInteger())

    return "${hexIp}:${hexPort}"
}

private updateDNI() { 
	//log.debug "In updateDNI()"
    
    //log.debug state.dni
    //log.debug device.deviceNetworkId
    
    if (device.deviceNetworkId != state.dni) {
        device.deviceNetworkId = state.dni
    }
    
    //log.debug state.dni
    //log.debug device.deviceNetworkId
}



private apiGet(String path) {
    LOG("apiGet(${path})")
	
//    log.debug "state.hostaddress"
//    log.debug state.hostaddress
    
    def headers = [
        HOST:       state.hostAddress,
        "Content-Type" : "application/json",
        Accept:     "*/*"
    ]

    def httpRequest = [
        method:     'GET',
        path:       path,
        headers:    headers
    ]

	updateDNI()

    return new physicalgraph.device.HubAction(httpRequest)
}

private apiPost(String path, String data) {
    //LOG("apiPost(${path}, ${data})")
	//	log.debug path
	//  log.debug data
    def headers = [
        HOST:       state.hostAddress,
        "Content-Type" : "application/json",
        Accept:     "*/*"
    ]

    def httpRequest = [
        method:     'POST',
        path:       path,
        headers:    headers,
        body:       data
    ]

	
	updateDNI()
    
//    log.debug headers
//    log.debug httpRequest
    
    return new physicalgraph.device.HubAction(httpRequest)
}

private def writeTstatValue(name, value) {
    LOG("writeTstatValue(${name}, ${value})")

    def json = "{\"${name}\": ${value}}"
    def hubActions = [
        apiPost("/tstat", json),
        delayHubAction(2000),
        apiGet("/tstat")
    ]

    
    return hubActions
}

private def delayHubAction(ms) {
    return new physicalgraph.device.HubAction("delay ${ms}")
}

private parseHttpHeaders(String headers) {
	// log.debug "In parseHttpHeaders"
    def lines = headers.readLines()
    def status = lines.remove(0).split()

    def result = [
        protocol:   status[0],
        status:     status[1].toInteger(),
        reason:     status[2]
    ]

    return result
}

private def parseTstatData(tstat) {

    LOG("parseTstatData(${tstat})")
	
    def events = []
    /*
    if (tstat.containsKey("error_msg")) {
        log.error "Thermostat error: ${tstat.error_msg}"
        return null
    }
    //new code DT
	if (tstat.containsKey("error")) {
        log.error "Smappee error: ${tstat.error}"
        return null
    }
    //end of new code DT
    if (tstat.containsKey("success")) {
        // this is POST response - ignore
        return null
    }
*/



		log.debug tstat.key [1]
        log.debug tstat.value [1]
        log.debug tstat.key [4]
  	  	log.debug tstat.value [4]
        log.debug tstat.key [7]
  	  	log.debug tstat.value [7]
    
    
    	def currentPower = tstat.value[1] as double
        def currentSolar = tstat.value[4] as double
        def currentLoad = tstat.value[7] as double
        def power_str = (tstat.value[1]).toFloat()
	LOG("power_str (${power_str})")
        currentPower=currentPower/1000
        currentPower=currentPower.round(0)
        
        currentSolar=currentSolar/1000
        currentSolar=currentSolar.round(0)
        
        currentLoad=currentLoad/1000
        currentLoad=currentLoad.round(0)
        
        /*currentGrid = tstat.value[0]/1000
        currentSolar = tstat.value[3]/1000
        currentPower = tstat.value[6]/1000*/
        
        LOG("power (${currentPower})")
        LOG("solar (${currentSolar})")
        LOG("load (${currentLoad})")
        
   def currentPowerMode
        
        if (currentPower < 0) {
        
        	currentPowerMode = "Export"
        	}
        	else {
            currentPowerMode = "Import"
                        }
            
         LOG("mode (${currentPowerMode})")   
        
        
		delayBetween([sendEvent(name: 'power', value: Math.round(currentPower), unit: "W"),
        	sendEvent(name: 'solar', value: Math.round(currentSolar), unit: "W"),
            sendEvent(name: 'load', value: Math.round(currentLoad), unit: "W"),
            sendEvent(name: 'powerMode', value: currentPowerMode)
		]) 

        
    
    /*
    
    sendEvent([name: "power", value: Math.round(results.power * 12), unit: "W"])
    
	if (tstat.containsKey("phase1ActivePower")) {
        
        def ev = [
            name:   "solar",
            value:  tstat.value [3]
        
        ]

        events << createEvent(ev)
    }
    if (tstat.containsKey("phase2ActivePower")) {
        
        def ev = [
            name:   "power",
            value:  tstat.value [6]
        
        ]

        events << createEvent(ev)
    }
    
    if (tstat.containsKey("t_cool")) {
        def ev = [
            name:   "coolingSetpoint",
            value:  scaleTemperature(tstat.t_cool.toFloat()),
            unit:   getTemperatureScale(),
        ]

        events << createEvent(ev)
    }

    if (tstat.containsKey("t_heat")) {
        def ev = [
            name:   "heatingSetpoint",
            value:  scaleTemperature(tstat.t_heat.toFloat()),
            unit:   getTemperatureScale(),
        ]

        events << createEvent(ev)
    }

    if (tstat.containsKey("tstate")) {
        def value = parseThermostatState(tstat.tstate)
        if (device.currentState("thermostatOperatingState")?.value != value) {
            def ev = [
                name:   "thermostatOperatingState",
                value:  value
            ]

            events << createEvent(ev)
        }
    }

    if (tstat.containsKey("fstate")) {
        def value = parseFanState(tstat.fstate)
        if (device.currentState("fanState")?.value != value) {
            def ev = [
                name:   "fanState",
                value:  value
            ]

            events << createEvent(ev)
        }
    }

    if (tstat.containsKey("tmode")) {
        def value = parseThermostatMode(tstat.tmode)
        if (device.currentState("thermostatMode")?.value != value) {
            def ev = [
                name:   "thermostatMode",
                value:  value
            ]

            events << createEvent(ev)
        }
    }

    if (tstat.containsKey("fmode")) {
        def value = parseFanMode(tstat.fmode)
        if (device.currentState("thermostatFanMode")?.value != value) {
            def ev = [
                name:   "thermostatFanMode",
                value:  value
            ]

            events << createEvent(ev)
        }
    }

    if (tstat.containsKey("hold")) {
        def value = parseThermostatHold(tstat.hold)
        if (device.currentState("hold")?.value != value) {
            def ev = [
                name:   "hold",
                value:  value
            ]

            events << createEvent(ev)
        }
    }
*/
    LOG("events: ${events}")
    return events
}

private def parseThermostatState(val) {
    def values = [
        "idle",     // 0
        "heating",  // 1
        "cooling"   // 2
    ]

    return values[val.toInteger()]
}

private def parseFanState(val) {
    def values = [
        "off",      // 0
        "on"        // 1
    ]

    return values[val.toInteger()]
}

private def parseThermostatMode(val) {
    def values = [
        "off",      // 0
        "heat",     // 1
        "cool",     // 2
        "auto"      // 3
    ]

    return values[val.toInteger()]
}

private def parseFanMode(val) {
    def values = [
        "auto",     // 0
        "circulate",// 1 (not supported by CT30)
        "on"        // 2
    ]

    return values[val.toInteger()]
}

private def parseThermostatHold(val) {
    def values = [
        "off",      // 0
        "on"        // 1
    ]

    return values[val.toInteger()]
}

private def scaleTemperature(Float temp) {
    if (getTemperatureScale() == "C") {
        return temperatureFtoC(temp)
    }

    return temp.round(1)
}

private def temperatureCtoF(Float tempC) {
    Float t = (tempC * 1.8) + 32
    return t.round(1)
}

private def temperatureFtoC(Float tempF) {
    Float t = (tempF - 32) / 1.8
    return t.round(1)
}




private def textVersion() {
    return "Version 1.0.3 (08/25/2015)"
}

private def textCopyright() {
    return "Copyright (c) 2014 Statusbits.com"
}

private def LOG(message) {
    log.trace message
}

private def STATE() {
	log.debug "In STATE()"
    log.trace "state: ${state}"
    log.trace "deviceNetworkId: ${device.deviceNetworkId}"
    log.trace "load: ${device.currentValue("load")}"
    log.trace "power: ${device.currentValue("power")}"
    log.trace "solar: ${device.currentValue("solar")}"
    log.trace "powerMode: ${device.currentValue("powerMode")}"

}

def logOnSmappee() {
	def headers = [:]
    headers.put ("HOST", "192.168.0.196:80")
    headers.put ("Content-Type", "application/json")
    def hubAction = new physicalgraph.device.HubAction(
    	method : "POST",
        path : "/gateway/apipublic/logon",
        body : "admin",
        headers : headers
        )
        
        log.debug hubAction
        hubAction
}
