-- Export a chunk sheet with settings required for my import code

local spr = app.activeSprite
if not spr then return print('No active sprite') end

local path,title = spr.filename:match("^(.+[/\\])(.-).([^.]*)$")

app.command.ExportSpriteSheet{
  ui=false,
  askOverwrite=true,
  type=SpriteSheetType.VERTICAL,
  bestFit=true,
  textureFilename=path .. '/export/' .. title .. '-sheet.png',
  dataFilename=path .. '/export/' .. title .. '-sheet.json',
  dataFormat=SpriteSheetDataFormat.JSON_ARRAY,
  trim=false,
  splitLayers=true,
  listLayers=false,
  listTags=false,
  listSlices=false,
  filenameFormat='{layer}',
}