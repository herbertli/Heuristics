Botty McBotFace
====================

Both clients are in sctm_bottymcbotface.py

The submarine captain is class SubbyMcSubFace
The trench manager is class TrenchyMcTrenchFace

You can import it like so:
from clients.sctm_bottymcbotface import TrenchyMcTrenchFace, SubbyMcSubFace

And just like the examples, you instantiate them like:
def init_submarine_captain(name, is_manual_mode, fd):
    sleep(1)
    player = SubbyMcSubFace(
        name=name) if not is_manual_mode else ManualSubmarineCaptain(name=name, fd=fd)
    player.play_game()


def init_trench_manager(name, is_manual_mode, fd):
    sleep(1)
    player = TrenchyMcTrenchFace(
        name=name) if not is_manual_mode else ManualTrenchManager(name=name, fd=fd)
    player.play_game()
