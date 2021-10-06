-- Export a sprite sheet with settings required for my import code

local spr = app.activeSprite
if not spr then return print('No active sprite') end

local path,title = spr.filename:match("^(.+[/\\])(.-).([^.]*)$")

app.command.ExportSpriteSheet{
  ui=false,
  askOverwrite=true,
  type=SpriteSheetType.PACKED,
  bestFit=true,
  textureFilename=path .. title .. '.png',
  dataFilename=path .. title .. '.json',
  dataFormat=SpriteSheetDataFormat.JSON_ARRAY,
  trim=true,
  listLayers=false,
  listTags=true,
  listSlices=true,
  filenameFormat='{frame}',
}