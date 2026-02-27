CREATE OR REPLACE VIEW land_cycles_view AS

SELECT DISTINCT c.id,
                c.name,
                c.type_line,
                'Shock Land' AS group_name
FROM card c
WHERE (
    c.name ILIKE '%Hallowed Fountain%'
        OR c.name ILIKE '%Steam Vents%'
        OR c.name ILIKE '%Blood Crypt%'
        OR c.name ILIKE '%Stomping Ground%'
        OR c.name ILIKE '%Temple Garden%'
        OR c.name ILIKE '%Watery Grave%'
        OR c.name ILIKE '%Godless Shrine%'
        OR c.name ILIKE '%Breeding Pool%'
        OR c.name ILIKE '%Overgrown Tomb%'
        OR c.name ILIKE '%Sacred Foundry%'
    )
  AND (c.type_line ILIKE '%Land%')

UNION ALL

SELECT DISTINCT c.id,
                c.name,
                c.type_line,
                'Triome' AS group_name
FROM card c
WHERE (
    c.name ILIKE '%Indatha Triome%'
        OR c.name ILIKE '%Ketria Triome%'
        OR c.name ILIKE '%Raugrin Triome%'
        OR c.name ILIKE '%Savai Triome%'
        OR c.name ILIKE '%Zagoth Triome%'
        OR c.name ILIKE '%Jetmir%'
        OR c.name ILIKE '%Raffine%'
        OR c.name ILIKE '%Spara%'
        OR c.name ILIKE '%Xander%'
        OR c.name ILIKE '%Ziatora%'
    )
  AND (c.type_line ILIKE '%Land%')

UNION ALL

SELECT DISTINCT c.id,
                c.name,
                c.type_line,
                'Bond Land' AS group_name
FROM card c
WHERE (
    c.name ILIKE '%Sea of Clouds%'
        OR c.name ILIKE '%Morphic Pool%'
        OR c.name ILIKE '%Luxury Suite%'
        OR c.name ILIKE '%Spire Garden%'
        OR c.name ILIKE '%Bountiful Promenade%'
        OR c.name ILIKE '%Spectator Seating%'
        OR c.name ILIKE '%Training Center%'
        OR c.name ILIKE '%Undergrowth Stadium%'
        OR c.name ILIKE '%Rejuvenating Springs%'
        OR c.name ILIKE '%Vault of Champions%'
    )
  AND (c.type_line ILIKE '%Land%')

UNION ALL

SELECT DISTINCT c.id,
                c.name,
                c.type_line,
                'Fetch Land' AS group_name
FROM card c
WHERE (
    c.name ILIKE '%Arid Mesa%'
        OR c.name ILIKE '%Misty Rainforest%'
        OR c.name ILIKE '%Scalding Tarn%'
        OR c.name ILIKE '%Verdant Catacombs%'
        OR c.name ILIKE '%Marsh Flats%'
        OR c.name ILIKE '%Bloodstained Mire%'
        OR c.name ILIKE '%Flooded Strand%'
        OR c.name ILIKE '%Polluted Delta%'
        OR c.name ILIKE '%Windswept Heath%'
        OR c.name ILIKE '%Wooded Foothills%'
    )
  AND (c.type_line ILIKE '%Land%')

UNION ALL

SELECT DISTINCT c.id,
                c.name,
                c.type_line,
                'Verge Land' AS group_name
FROM card c
WHERE (
    c.name ILIKE '%Floodfarm Verge%'
        OR c.name ILIKE '%Gloomlake Verge%'
        OR c.name ILIKE '%Blazemire Verge%'
        OR c.name ILIKE '%Thornspire Verge%'
        OR c.name ILIKE '%Hushwood Verge%'
        OR c.name ILIKE '%Bleachbone Verge%'
        OR c.name ILIKE '%Riverpyre Verge%'
        OR c.name ILIKE '%Sunbillow Verge%'
        OR c.name ILIKE '%Wastewood Verge%'
        OR c.name ILIKE '%Willowrush Verge%'
    )
  AND (c.type_line ILIKE '%Land%')

UNION ALL

SELECT DISTINCT c.id,
                c.name,
                c.type_line,
                'Surveil Land' AS group_name
FROM card c
WHERE (
    c.name ILIKE '%Commercial District%'
        OR c.name ILIKE '%Elegant Parlor%'
        OR c.name ILIKE '%Hedge Maze%'
        OR c.name ILIKE '%Lush Portico%'
        OR c.name ILIKE '%Meticulous Archive%'
        OR c.name ILIKE '%Raucous Theater%'
        OR c.name ILIKE '%Shadowy Backstreet%'
        OR c.name ILIKE '%Thundering Falls%'
        OR c.name ILIKE '%Undercity Sewers%'
        OR c.name ILIKE '%Underground Mortuary%'
    )
  AND (c.type_line ILIKE '%Land%')

UNION ALL

SELECT DISTINCT c.id,
                c.name,
                c.type_line,
                'ABU Dual Land' AS group_name
FROM card c
WHERE c.name IN (
                 'Badlands',
                 'Bayou',
                 'Plateau',
                 'Savannah',
                 'Scrubland',
                 'Taiga',
                 'Tropical Island',
                 'Tundra',
                 'Underground Sea',
                 'Volcanic Island'
    )
  AND (c.type_line ILIKE '%Land%')

UNION ALL

SELECT DISTINCT c.id,
                c.name,
                c.type_line,
                'Pathway Land' AS group_name
FROM card c
WHERE (
    c.name ILIKE '%Needleverge Pathway%'
        OR c.name ILIKE '%Riverglide Pathway%'
        OR c.name ILIKE '%Brightclimb Pathway%'
        OR c.name ILIKE '%Clearwater Pathway%'
        OR c.name ILIKE '%Cragcrown Pathway%'
        OR c.name ILIKE '%Barkchannel Pathway%'
        OR c.name ILIKE '%Blightstep Pathway%'
        OR c.name ILIKE '%Darkbore Pathway%'
        OR c.name ILIKE '%Hengegate Pathway%'
        OR c.name ILIKE '%Branchloft Pathway%'
    )
  AND (c.type_line ILIKE '%Land%')

UNION ALL

SELECT DISTINCT c.id,
                c.name,
                c.type_line,
                'Check Land' AS group_name
FROM card c
WHERE (
    c.name ILIKE '%Clifftop Retreat%'
        OR c.name ILIKE '%Glacial Fortress%'
        OR c.name ILIKE '%Hinterland Harbor%'
        OR c.name ILIKE '%Isolated Chapel%'
        OR c.name ILIKE '%Sulfur Falls%'
        OR c.name ILIKE '%Dragonskull Summit%'
        OR c.name ILIKE '%Drowned Catacomb%'
        OR c.name ILIKE '%Rootbound Crag%'
        OR c.name ILIKE '%Sunpetal Grove%'
        OR c.name ILIKE '%Woodland Cemetery%'
    )
  AND (c.type_line ILIKE '%Land%')

ORDER BY group_name,
         name;