from PIL import Image, ImageDraw
from pathlib import Path
out=Path(__file__).resolve().parent
im=Image.new("RGBA",(48,48),(0,0,0,0));d=ImageDraw.Draw(im)
ink="#38352f";gold="#eab94e";light="#ffe395";shade="#b88132"
# A small library: three books on a single shelf, drawn on the pixel grid.
d.rectangle((7,10,17,35),fill=ink);d.rectangle((9,12,15,33),fill=gold)
d.rectangle((9,12,10,33),fill=light);d.rectangle((14,12,15,33),fill=shade)
d.rectangle((10,16,14,17),fill=ink);d.rectangle((10,28,14,29),fill=ink)
d.rectangle((19,7,29,35),fill=ink);d.rectangle((21,9,27,33),fill="#8cad97")
d.rectangle((21,9,22,33),fill="#c2d9af");d.rectangle((26,9,27,33),fill="#567f73")
d.rectangle((22,13,26,14),fill=ink);d.rectangle((22,28,26,29),fill=ink)
d.polygon([(31,12),(39,10),(44,33),(35,35)],fill=ink)
d.polygon([(33,14),(37,13),(41,31),(37,32)],fill=gold)
d.line([(33,14),(37,31)],fill=light,width=1)
d.rectangle((5,35,44,39),fill=ink);d.rectangle((7,35,42,36),fill="#b79669")
im.save(out/"rinlib-icon-48.png")
im.resize((576,576),Image.Resampling.NEAREST).save(out/"rinlib-icon.png")
