#!/usr/bin/env python3

import aiohttp
import asyncio

from renault_api.renault_client import RenaultClient

async def getStat(username, password, accountId, vin):
   async with aiohttp.ClientSession() as websession:
      client = RenaultClient(websession=websession, locale="fr_FR")
      await client.session.login(username, password)

      account = await client.get_api_account(accountId)

      vehicle = await account.get_api_vehicle(vin)
      response_battery = await vehicle.get_battery_status()

      return f'"timestamp":"{response_battery.timestamp}", "batteryLevel":{response_battery.batteryLevel}, "batteryTemperature":{response_battery.batteryTemperature}, "batteryAutonomy":{response_battery.batteryAutonomy}, "batteryAvailableEnergy":{response_battery.batteryAvailableEnergy}, "plugStatus":{response_battery.plugStatus}, "chargingStatus":{response_battery.chargingStatus}, "chargingRemainingTime":{response_battery.chargingRemainingTime}, "chargingInstantaneousPower":{response_battery.chargingInstantaneousPower}'

async def getKameo(username, password):
   async with aiohttp.ClientSession() as websession:
      client = RenaultClient(websession=websession, locale="fr_FR")
      await client.session.login(username, password)

      response = await client.get_person()
      return response.accounts[0].accountId
   
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

def get_stat(username, password, accountId, vin):
   loop = asyncio.get_event_loop()
   return loop.run_until_complete(getStat(username, password, accountId, vin))

def get_key(username, password):
   loop = asyncio.get_event_loop()
   return loop.run_until_complete(getKameo(username, password))

def get_vehicles(username, password, accountId):
   loop = asyncio.get_event_loop()
   return loop.run_until_complete(getVehicles(username, password, accountId))
