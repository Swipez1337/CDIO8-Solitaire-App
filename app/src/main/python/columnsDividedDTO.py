import json
import layoutMatches

# @author s183925
def getJsonList(columnsDivided):
    if columnsDivided is -1:
        return - 1
    jsonList = [[], [], [], [], [], [], [], [], [], [], [], []]
    for index in range(len(columnsDivided)):
        for card in columnsDivided[index]:
            jsonList[index].append(card.name)
    DTO = json.dumps(jsonList)
    return DTO