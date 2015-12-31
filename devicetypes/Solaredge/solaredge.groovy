/**
 *  Solaredge Solar Energy Monitoring System
 *
 *  Copyright 2015 David Tucker based on original code from by Carlos Santiago, Ronald Gouldner
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
 
preferences {

	section("Enter Configuration data for your Solaredge Inverter") {


	input("uri", "text", title: "Solaredge Monitor API URL", description: "https://monitoringapi.solaredge.com", required: false)
    input("siteId", "text", title: "Site Id", required: true)
    input("apiKey", "text", title: "API Key (get this from your Solar PV installer)", required: true)
    }
}

metadata {
	definition (name: "Solaredge System", namespace: "strelitzia123", author: "David Tucker") {
	capability "Power Meter"
    capability "Energy Meter"
    capability "Refresh"
	//capability "Polling"
        
    attribute "energy_today", "string"
	attribute "energy_month", "number"
	attribute "energy_year", "number"
	attribute "energy_lifetime", "number"
    attribute "production_level", "number"
    
        
    fingerprint deviceId: "Solaredge"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
            valueTile("solar", "device.power") {
   	         state("solarPower", label: '${currentValue}\nSolar', unit:"W", backgroundColors: [
                    [value: 2, color: "#32CD32"],
                    [value: 1, color: "#000000"],
    	            ]
                )
        	}
             valueTile("energy_today", "device.energy_today") {
   	         state("energy_today", label: '${currentValue}\nToday', unit:"KWh", backgroundColors: [
                    [value: 1, color: "#bc2323"],
                    [value: 2, color: "#d04e00"],
                    [value: 5, color: "#f1d801"],
                    [value: 10, color: "#90d2a7"],
		            [value: 15, color: "#44b621"],
                    [value: 20, color: "#1e9cbb"],
                    [value: 25, color: "#153591"]
    	            ]
            	)
            }
            valueTile("energy_month", "device.energy_month") {
   	         state("energy_month", label: '${currentValue}\nMonth', unit:"KWh", backgroundColors: [
                    [value: 30, color: "#bc2323"],
                    [value: 60, color: "#d04e00"],
                    [value: 150, color: "#f1d801"],
                    [value: 300, color: "#90d2a7"],
		            [value: 450, color: "#44b621"],
                    [value: 600, color: "#1e9cbb"],
                    [value: 750, color: "#153591"]
    	            ]
            	)
            }
            valueTile("energy_year", "device.energy_year") {
   	         state("energy_year", label: '${currentValue}\nYear', unit:"MWh")
            }
            valueTile("energy_lifetime", "device.energy_lifetime") {
   	         state("energy_lifetime", label: '${currentValue}\nLifetime', unit:"MWh")
            }
            valueTile("productionLevel", "device.production_level") {
				state("productionLevel", label: '${currentValue}%\nProduction', unit:"%", backgroundColor: "#0000FF")
			}
          

            standardTile("refresh", "device.energy_today", inactiveLabel: false, decoration: "flat") {
                state "default", action:"polling.poll", icon:"st.secondary.refresh"
            }

        
        main (["solar"])
        details(["solar", 
//        "productionLevel", 															// TODO coding for production level %
        "energy_today", "energy_month", "energy_year", "energy_lifetime","refresh"])

	}
}


// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

}

def poll() {
	//refresh()
}

def refresh() {
  log.debug "Executing 'refresh'"
  energyRefresh()
}


def energyRefresh() {  
  log.debug "Executing 'energyToday'"
  
  def cmd = "${settings.uri}/site/${settings.siteId}/overview?api_key=${settings.apiKey}";
  log.debug "Sending request cmd[${cmd}]"
  
  httpGet(cmd) {resp ->
        if (resp.data) {
        	log.debug "${resp.data}"
            
            def currentsolarPower = 0
            currentsolarPower = resp.data.overview.currentPower.power
            log.debug "currentsolarPower :${currentsolarPower}"
            
            def energyToday = resp.data.overview.lastDayData.energy/1000
            log.debug "energyToday :${energyToday}"            
      
            
            log.debug "${resp.data.overview.lastMonthData.energy}"
			def energyMonth = resp.data.overview.lastMonthData.energy/1000
            log.debug "energyMonth :${energyMonth}" 
            
			def energyYear = resp.data.overview.lastYearData.energy/1e6
            log.debug "energyYear :${energyYear}" 
            
            def energyLifetime = resp.data.overview.lifeTimeData.energy/1e6
            log.debug "energyLifetime :${energyLifetime}"

			
            delayBetween(
               	[sendEvent(name: 'power', value: (currentsolarPower))
                ,sendEvent(name: 'energy_today', value: (String.format("%5.2f", energyToday)))
                ,sendEvent(name: 'energy_month', value: (String.format("%5.2f", energyMonth)))
                ,sendEvent(name: 'energy_year', value: (String.format("%5.2f", energyYear)))
                ,sendEvent(name: 'energy_lifetime', value: (String.format("%5.2f", energyLifetime)))
             	]
            )				
            


        }
        
    }
}
