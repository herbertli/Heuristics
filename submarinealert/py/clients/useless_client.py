import json
from random import randint, choice

from clients.trench_manager_client import TrenchManager


class UTrenchManager(TrenchManager):
    def __init__(self):
        super().__init__("Trenchy McTrenchFace")

    def send_probes(self):
        return self.getProbes()

    def choose_alert(self, sent_probes, results):
        self.receiveProbeResults(results)
        return "red" if self.shouldGoRed() else "yellow"

    def getProbes(self) -> list:
        return []

    def receiveProbeResults(self, results: list) -> None:
        return

    def shouldGoRed(self) -> bool:
        return True

