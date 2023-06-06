from os import listdir, remove, path

PATH = '..\\..\\..\\media'
EXT = '.png'
FRAMES_PATH = PATH + '\\frames'

for file in listdir(FRAMES_PATH):
    if file.endswith(EXT):
        remove(path.join(FRAMES_PATH, file))
