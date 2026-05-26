#!/usr/bin/env python3

import aiohttp
import asyncio
import json

from renault_api.renault_client import RenaultClient

async def getStat(token, accountId, vin):
   async with aiohttp.ClientSession() as websession:
      client = RenaultClient(websession=websession, locale="fr_FR")
      client.session.set_login_token(token)

      account = await client.get_api_account(accountId)

      vehicle = await account.get_api_vehicle("VF1AG000868830928")
      response_battery = await vehicle.get_battery_status()

      return vin
      return f'"timestamp":"{response_battery.timestamp}", "batteryLevel":{response_battery.batteryLevel}, "batteryTemperature":{response_battery.batteryTemperature}, "batteryAutonomy":{response_battery.batteryAutonomy}, "batteryAvailableEnergy":{response_battery.batteryAvailableEnergy}, "plugStatus":{response_battery.plugStatus}, "chargingStatus":{response_battery.chargingStatus}, "chargingRemainingTime":{response_battery.chargingRemainingTime}, "chargingInstantaneousPower":{response_battery.chargingInstantaneousPower}'

async def getKameo(username, password):
   async with aiohttp.ClientSession() as websession:
      client = RenaultClient(websession=websession, locale="fr_FR")
      await client.session.login(username, password)

      person = await client.get_person()
      account = await client.get_api_account(person.accounts[0].accountId)
      vehicles = await account.get_vehicles()
      vehicle_list = []

      for x in vehicles.vehicleLinks:
         vehicle_list.append(x.vin)

      data = {
         "id": person.accounts[0].accountId,
         "token": client.session.login_token,
         "vehicles": vehicle_list
      }
      
      return json.dumps(data)
   
async def getVehicles(username, password, accountId):
   async with aiohttp.ClientSession() as websession:
      client = RenaultClient(websession=websession, locale="fr_FR")
      await client.session.login(username, password)

      account = await client.get_api_account(accountId)
      vehicles = await account.get_vehicles()

      vehicle_list = []

      for x in vehicles.vehicleLinks:
         vehicle_list.append(x.vin)

      return vehicle_list

def login(username, password):
   loop = asyncio.get_event_loop()
   return loop.run_until_complete(getKameo(username, password))

def get_vehicles(username, token, accountId):
   loop = asyncio.get_event_loop()
   return loop.run_until_complete(getVehicles(username, token, accountId))

def get_stat(token, accountId, vin):
   loop = asyncio.get_event_loop()
   return loop.run_until_complete(getStat(token, accountId, vin))
