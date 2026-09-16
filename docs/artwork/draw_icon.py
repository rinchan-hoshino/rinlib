from PIL import Image, ImageDraw
from pathlib import Path
out = Path(__file__).resolve().parent
im = Image.new('RGBA', (48, 48), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
ink = '#493e32'
# Original geometric white hair bow, with Rin-yellow knot and restrained shading.
d.polygon([(7,9),(12,9),(23,19),(25,19),(36,9),(41,9),(41,14),(37,25),(28,27),(33,36),(32,40),(25,37),(24,32),(23,37),(16,40),(15,36),(20,27),(11,25),(7,14)], fill=ink)
d.polygon([(10,12),(12,12),(22,21),(21,24),(13,22),(10,14)], fill='#fff8dd')
d.polygon([(36,12),(38,12),(38,14),(35,22),(27,24),(26,21)], fill='#fff8dd')
d.polygon([(13,20),(21,22),(21,24),(13,22)], fill='#d6d0ba')
d.polygon([(27,22),(35,20),(35,22),(27,24)], fill='#d6d0ba')
d.polygon([(22,27),(23,28),(21,35),(18,37)], fill='#fff8dd')
d.polygon([(25,28),(26,27),(30,37),(27,35)], fill='#fff8dd')
d.rectangle((21,19,27,27), fill=ink)
d.rectangle((23,21,25,25), fill='#f3bd43')
d.rectangle((23,21,25,22), fill='#ffe58c')
im.save(out/'rinlib-icon-48.png')
im.resize((576,576), Image.Resampling.NEAREST).save(out/'rinlib-icon.png')

platform = Image.new("RGBA", (400, 400))
platform.paste(im.resize((384, 384), Image.Resampling.NEAREST), (8, 8))
platform.save(out / "rinlib-icon-platform.png")
