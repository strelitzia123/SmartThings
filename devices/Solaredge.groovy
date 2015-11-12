/**
 *  Solaredge Solar Energy Monitoring System
 *
 *  Copyright 2015 David Tucker based on original version by Carlos Santiago, Ronald Gouldner
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
	definition (name: "Solaredge System", namespace: "davidjtucker", author: "David Tucker") {
	capability "Power Meter"
    capability "Energy Meter"
    capability "Refresh"
	capability "Polling"
        
    attribute "energy_today", "STRING"
	attribute "energy_month", "number"
	attribute "energy_year", "STRING"
	attribute "energy_lifetime", "number"
    attribute "production_level", "STRING"
    
        
    fingerprint deviceId: "Solaredge"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
            valueTile("solar", "device.power") {
   	         state("solarPower", label: '${currentValue}W\nSolar', unit:"W", backgroundColors: [
                    [value: 1, color: "#32CD32"],
                    [value: 0, color: "#000000"],
    	            ]
                )
        	}
             valueTile("energy_today", "device.energy") {
   	         state("energy", label: '${currentValue}K\nToday', unit:"KWh", backgroundColors: [
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
            valueTile("productionLevel", "device.production_level") {
				state("productionLevel", label: '${currentValue}%\nProduction', unit:"%", backgroundColor: "#0000FF")
			}
            /*
            valueTile("grid", "device.gridpower") {
   	         state("gridPower", label: '${currentValue}W\nGrid', unit:"W", backgroundColors: [
                    [value: -1, color: "#32CD32"],
                    [value: 1, color: "#FF0000"],
    	            ]
                )
        	} 
            */

            standardTile("refresh", "device.energy_today", inactiveLabel: false, decoration: "flat") {
                state "default", action:"polling.poll", icon:"st.secondary.refresh"
            }

        
        main (["solar"])
        details(["solar", "productionLevel", "energy_today", "refresh"])

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
            
            log.debug "${resp.data.overview.lastMonthData.energy}"
			def energy_month = resp.data.overview.lastMonthData.energy
            log.debug "energy_month :${energy_month}" 
            
			def energy_year = resp.data.overview.lastYearData.energy
            log.debug "energy_year :${energy_year}" 
            
            def energy_today = resp.data.overview.lastDayData.energy
            log.debug "energy_today :${energy_today}"            
      
            def energy_lifetime = resp.data.overview.lifeTimeData.energy
            log.debug "energy_lifetime :${energy_lifetime}"

			def currentsolarPower = 0
            currentsolarPower = resp.data.overview.currentPower.power
            log.debug "currentsolarPower :${currentsolarPower}"
            
            delayBetween(
               	[sendEvent(name: 'energy', value: (energy_today))
                ,sendEvent(name: 'power', value: (currentsolarPower))
                
             	]
            )				
            

/*
			delayBetween([sendEvent(name: 'energy_today', value: (String.format("%5.2f", energyToday)))
                          ,sendEvent(name: 'energy_life', value: (String.format("%5.2f",energyLife)))
                          ,sendEvent(name: 'power', value: (currentPower))
						  ,sendEvent(name: 'production_level', value: (String.format("%5.2f",productionLevel)))
						  ,sendEvent(name: 'today_max_prod', value: (todayMaxProd))
						  ,sendEvent(name: 'today_max_prod_str', value: (String.format("%5.2f",todayMaxProd)))
						  ,sendEvent(name: 'reported_id', value: (systemId))
	                     ])
            def energyToday = 0

                         
             // String.format("%5.2f", energyToday)
             	delayBetween([sendEvent(name: 'gridpower', value: (currentgridPower))
                				,sendEvent(name: 'power', value: (currentsolarPower))
             		])				

        }
        if(resp.status == 200) {
            	log.debug "poll results returned"
        }
         else {
            log.error "polling children & got http status ${resp.status}"
            */
        }
        
    }
}
