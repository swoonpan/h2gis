-- Copyright © 2007 Open Geospatial Consortium, Inc. All Rights Reserved. 

CREATE TABLE spatial_ref_sys (

srid INTEGER NOT NULL PRIMARY KEY,

auth_name CHARACTER VARYING,

auth_srid INTEGER,

srtext CHARACTER VARYING(2048));

-- Geometry Columns

CREATE TABLE geometry_columns (

f_table_schema CHARACTER VARYING,

f_table_name CHARACTER VARYING,

f_geometry_column CHARACTER VARYING,

g_table_schema CHARACTER VARYING,

g_table_name CHARACTER VARYING,

storage_type INTEGER,

geometry_type INTEGER,

coord_dimension INTEGER,

max_ppr INTEGER,

srid INTEGER DEFAULT (SELECT srid from spatial_ref_sys LIMIT 1) REFERENCES spatial_ref_sys ,

CONSTRAINT gc_pk PRIMARY KEY (f_table_name,

f_geometry_column));

-- Lake Geometry

CREATE TABLE lake_geom (

gid INTEGER NOT NULL PRIMARY KEY,

xmin INTEGER,

ymin INTEGER,

xmax INTEGER,

ymax INTEGER,

wkbgeometry VARBINARY);

-- Road Segment Geometry

CREATE TABLE road_segment_geom (

gid INTEGER NOT NULL PRIMARY KEY,

xmin INTEGER,

ymin INTEGER,

xmax INTEGER,

ymax INTEGER,

wkbgeometry VARBINARY);

-- Divided Route Geometry

CREATE TABLE divided_route_geom (

gid INTEGER NOT NULL PRIMARY KEY,

xmin INTEGER,

ymin INTEGER,

xmax INTEGER,

ymax INTEGER,

wkbgeometry VARBINARY);

-- Forest Geometry

CREATE TABLE forest_geom (

gid INTEGER NOT NULL PRIMARY KEY,

xmin INTEGER,

ymin INTEGER,

xmax INTEGER,

ymax INTEGER,

wkbgeometry VARBINARY);

-- Bridge Geometry

CREATE TABLE bridge_geom (

gid INTEGER NOT NULL PRIMARY KEY,

xmin INTEGER,

ymin INTEGER,

xmax INTEGER,

ymax INTEGER,

wkbgeometry VARBINARY);

-- Stream Geometry

CREATE TABLE stream_geom (

gid INTEGER NOT NULL PRIMARY KEY,

xmin INTEGER,

ymin INTEGER,

xmax INTEGER,

ymax INTEGER,

wkbgeometry VARBINARY);

-- Bulding Point Geometry

CREATE TABLE building_pt_geom (

gid INTEGER NOT NULL PRIMARY KEY,

xmin INTEGER,

ymin INTEGER,

xmax INTEGER,

ymax INTEGER,

wkbgeometry VARBINARY);

-- Bulding Area Geometry

CREATE TABLE building_area_geom (

gid INTEGER NOT NULL PRIMARY KEY,

xmin INTEGER,

ymin INTEGER,

xmax INTEGER,

ymax INTEGER,

wkbgeometry VARBINARY);

-- Pond Geometry

CREATE TABLE pond_geom (

gid INTEGER NOT NULL PRIMARY KEY,

xmin INTEGER,

ymin INTEGER,

xmax INTEGER,

ymax INTEGER,

wkbgeometry VARBINARY);

-- Named Place Geometry

CREATE TABLE named_place_geom (

gid INTEGER NOT NULL PRIMARY KEY,

xmin INTEGER,

ymin INTEGER,

xmax INTEGER,

ymax INTEGER,

wkbgeometry VARBINARY);

-- Map Neatline Geometry

CREATE TABLE map_neatline_geom (

gid INTEGER NOT NULL PRIMARY KEY,

xmin INTEGER,

ymin INTEGER,

xmax INTEGER,

ymax INTEGER,

wkbgeometry VARBINARY);

-- Lakes

CREATE TABLE lakes (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

shore_gid INTEGER);

-- Road Segments

CREATE TABLE road_segments (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

aliases CHARACTER VARYING(64),

num_lanes INTEGER,

centerline_gid INTEGER);

-- Divided Routes

CREATE TABLE divided_routes (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

num_lanes INTEGER,

centerlines_gid INTEGER);

-- Forests

CREATE TABLE forests (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

boundary_gid INTEGER);

-- Bridges

CREATE TABLE bridges (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

position_gid INTEGER);

-- Streams

CREATE TABLE streams (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

centerline_gid INTEGER);

-- Buildings

CREATE TABLE buildings (

fid INTEGER NOT NULL PRIMARY KEY,

address CHARACTER VARYING(64),

position_gid INTEGER,

footprint_gid INTEGER);

-- Ponds

CREATE TABLE ponds (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

type CHARACTER VARYING(64),

shores_gid INTEGER);

-- Named Places

CREATE TABLE named_places (

fid INTEGER NOT NULL PRIMARY KEY,

name CHARACTER VARYING(64),

boundary_gid INTEGER);

-- Map Neatline

CREATE TABLE map_neatlines (

fid INTEGER NOT NULL PRIMARY KEY,

neatline_gid INTEGER);

-- Spatial Reference Systems

INSERT INTO spatial_ref_sys VALUES(101, 'POSC', 32214,

'PROJCS["UTM_ZONE_14N", GEOGCS["World Geodetic System

72",DATUM["WGS_72", ELLIPSOID["NWL_10D", 6378135,

298.26]],PRIMEM["Greenwich",

0],UNIT["Meter",1.0]],PROJECTION["Transverse_Mercator"],

PARAMETER["False_Easting", 500000.0],PARAMETER["False_Northing",

0.0],PARAMETER["Central_Meridian", -99.0],PARAMETER["Scale_Factor",

0.9996],PARAMETER["Latitude_of_origin", 0.0],UNIT["Meter", 1.0]]');

-- Lakes

INSERT INTO lake_geom VALUES(101, 48.0, 6.0, 73.0, 23.0,

HEXTOVARBINARY('010300000002000000050000000000000000004a4000000000000032400000000000805040000000000000374000000000004052400000000000002240000000000000484000000000000018400000000000004a400000000000003240050000000000000000804d4000000000000032400000000000c0504000000000000032400000000000c050400000000000002a400000000000804d400000000000002a400000000000804d400000000000003240'));

INSERT INTO lakes VALUES (

101, 'BLUE LAKE', 101);

-- Road segments

INSERT INTO road_segment_geom VALUES (

101, 0.0, 18.0, 44.0, 31.0,

HEXTOVARBINARY('0102000000050000000000000000000000000000000000324000000000000024400000000000003540000000000000304000000000000037400000000000003c400000000000003a4000000000000046400000000000003f40'));

INSERT INTO road_segment_geom VALUES (

102, 44.0, 31.0, 70.0, 38.0,

HEXTOVARBINARY('01020000000300000000000000000046400000000000003f400000000000004c40000000000000414000000000008051400000000000004340'));

INSERT INTO road_segment_geom VALUES (

103, 70.0, 38.0, 72.0, 48.0,

HEXTOVARBINARY('0102000000020000000000000000805140000000000000434000000000000052400000000000004840'));

INSERT INTO road_segment_geom VALUES (

104, 70.0, 38.0, 84.0, 42.0,

HEXTOVARBINARY('0102000000020000000000000000805140000000000000434000000000000055400000000000004540'));

INSERT INTO road_segment_geom VALUES (

105, 28.0, 0.0, 28.0, 26.0,

HEXTOVARBINARY('0102000000020000000000000000805140000000000000434000000000000055400000000000004540'));

INSERT INTO road_segments VALUES(102, 'Route 5', NULL, 2, 101);

INSERT INTO road_segments VALUES(103, 'Route 5', 'Main Street', 4, 102);

INSERT INTO road_segments VALUES(104, 'Route 5', NULL, 2, 103);

INSERT INTO road_segments VALUES(105, 'Main Street', NULL, 4, 104);

INSERT INTO road_segments VALUES(106, 'Dirt Road by Green Forest', NULL,

1, 105);

-- DividedRoutes

INSERT INTO divided_route_geom VALUES(101, 10.0, 0.0, 16.0, 48.0,

HEXTOVARBINARY('010500000002000000010200000003000000000000000000244000000000000048400000000000002440000000000000354000000000000024400000000000000000010200000003000000000000000000304000000000000000000000000000002440000000000000374000000000000030400000000000004840'));

INSERT INTO divided_routes VALUES(119, 'Route 75', 4, 101);

-- Forests

INSERT INTO forest_geom VALUES(101, 28.0, 0.0, 84.0, 42.0,

HEXTOVARBINARY('010600000002000000010300000002000000050000000000000000003c400000000000003a400000000000003c40000000000000000000000000000055400000000000000000000000000000554000000000000045400000000000003c400000000000003a40050000000000000000004a4000000000000032400000000000805040000000000000374000000000004052400000000000002240000000000000484000000000000018400000000000004a400000000000003240010300000001000000050000000000000000804d4000000000000032400000000000c0504000000000000032400000000000c050400000000000002a400000000000804d400000000000002a400000000000804d400000000000003240'));

INSERT INTO forests VALUES(109, 'Green Forest', 101);

-- Bridges

INSERT INTO bridge_geom VALUES(101, 44.0, 31.0, 44.0, 31.0,

HEXTOVARBINARY('010100000000000000000046400000000000003f40'));

INSERT INTO bridges VALUES(110, 'Cam Bridge', 101);

-- Streams

INSERT INTO stream_geom VALUES(101, 38.0, 18.0, 52.0, 48.0,

HEXTOVARBINARY('01020000000500000000000000000043400000000000004840000000000000464000000000008044400000000000804440000000000000424000000000000046400000000000003f400000000000004a400000000000003240'));

INSERT INTO stream_geom VALUES(102, 73.0, 0.0, 78.0, 9.0,

HEXTOVARBINARY('010200000003000000000000000000534000000000000000000000000000805340000000000000104000000000004052400000000000002240'));

INSERT INTO streams VALUES(111, 'Cam Stream', 101);

INSERT INTO streams VALUES(112, 'Cam Stream', 102);

-- Buildings

INSERT INTO building_pt_geom VALUES(101, 52.0, 30.0, 52.0, 30.0,

HEXTOVARBINARY('01010000000000000000004a400000000000003e40'));

INSERT INTO building_pt_geom VALUES(102, 64.0, 33.0, 64.0, 33.0,

HEXTOVARBINARY('010100000000000000000050400000000000804040'));

INSERT INTO building_area_geom VALUES(101, 50.0, 29.0, 54.0, 31.0,

HEXTOVARBINARY('0103000000010000000500000000000000000049400000000000003f400000000000004b400000000000003f400000000000004b400000000000003d4000000000000049400000000000003d4000000000000049400000000000003f40'))

;

INSERT INTO building_area_geom VALUES(102, 62.0, 32.0, 66.0, 34.0,

HEXTOVARBINARY('01030000000100000005000000000000000080504000000000000041400000000000004f4000000000000041400000000000004f4000000000000040400000000000805040000000000000404000000000008050400000000000004140'))

;

INSERT INTO buildings VALUES(113, '123 Main Street', 101, 101);

INSERT INTO buildings VALUES(114, '215 Main Street', 102, 102);

-- Ponds

INSERT INTO pond_geom VALUES(101, 22.0, 40.0, 28.0, 44.0,

HEXTOVARBINARY('0106000000020000000103000000010000000400000000000000000038400000000000004640000000000000364000000000000045400000000000003840000000000000444000000000000038400000000000004640010300000001000000040000000000000000003a4000000000000046400000000000003a4000000000000044400000000000003c4000000000000045400000000000003a400000000000004640'));

INSERT INTO ponds VALUES(120, NULL, 'Stock Pond', 101);

-- Named Places

INSERT INTO named_place_geom VALUES(101, 56.0, 30.0, 84.0, 48.0,

HEXTOVARBINARY('010300000001000000060000000000000000004f4000000000000048400000000000005540000000000000484000000000000055400000000000003e400000000000004c400000000000003e400000000000004c4000000000000041400000000000004f400000000000004840'));

INSERT INTO named_place_geom VALUES(102, 59.0, 13.0, 67.0, 18.0,

HEXTOVARBINARY('010300000001000000050000000000000000c050400000000000002a400000000000c0504000000000000032400000000000804d4000000000000032400000000000804d400000000000002a400000000000c050400000000000002a40'))

;

INSERT INTO named_places VALUES(117, 'Ashton', 101);

INSERT INTO named_places VALUES(118, 'Goose Island', 102);

-- Map Neatlines

INSERT INTO map_neatline_geom VALUES(101, 0.0, 0.0, 84.0, 48.0,

HEXTOVARBINARY('010300000001000000050000000000000000000000000000000000000000000000000000000000000000004840000000000000554000000000000048400000000000005540000000000000000000000000000000000000000000000000'))

;

INSERT INTO map_neatlines VALUES(115, 101);

--Geometry Columns

INSERT INTO geometry_columns (f_table_name,f_geometry_column,g_table_name,storage_type, geometry_type, coord_dimension, max_ppr ) VALUES (

'lakes', 'shore_gid',

'lake_geom',1, 5, 2, 0);

INSERT INTO geometry_columns (f_table_name,f_geometry_column,g_table_name,storage_type, geometry_type, coord_dimension, max_ppr ) VALUES (

'road_segments',

'centerline_gid', 'road_segment_geom',1, 3, 2, 101);

INSERT INTO geometry_columns (f_table_name,f_geometry_column,g_table_name,storage_type, geometry_type, coord_dimension, max_ppr ) VALUES (

'divided_routes',

'centerlines_gid', 'divided_route_geom',1, 9, 2, 101);

INSERT INTO geometry_columns (f_table_name,f_geometry_column,g_table_name,storage_type, geometry_type, coord_dimension, max_ppr ) VALUES (

'forests', 'boundary_gid',

'forest_geom',1, 11, 2, 101);

INSERT INTO geometry_columns (f_table_name,f_geometry_column,g_table_name,storage_type, geometry_type, coord_dimension, max_ppr ) VALUES (

'bridges', 'position_gid',

'bridge_geom',1, 1, 2, 101);

INSERT INTO geometry_columns (f_table_name,f_geometry_column,g_table_name,storage_type, geometry_type, coord_dimension, max_ppr ) VALUES (

'streams', 'centerline_gid',

'stream_geom',1, 3, 2, 101);

INSERT INTO geometry_columns (f_table_name,f_geometry_column,g_table_name,storage_type, geometry_type, coord_dimension, max_ppr ) VALUES (

'buildings', 'position_gid',

'building_pt_geom',1, 1, 2, 101);

INSERT INTO geometry_columns (f_table_name,f_geometry_column,g_table_name,storage_type, geometry_type, coord_dimension, max_ppr ) VALUES (

'buildings', 'footprint_gid',

'building_area_geom',1, 5, 2, 101);

INSERT INTO geometry_columns (f_table_name,f_geometry_column,g_table_name,storage_type, geometry_type, coord_dimension, max_ppr ) VALUES (

'ponds', 'shores_gid',

'pond_geom',1, 11, 2, 101);

INSERT INTO geometry_columns (f_table_name,f_geometry_column,g_table_name,storage_type, geometry_type, coord_dimension, max_ppr ) VALUES (

'named_places', 'boundary_gid',

'named_place_geom',1, 5, 2, 101);

INSERT INTO geometry_columns (f_table_name,f_geometry_column,g_table_name,storage_type, geometry_type, coord_dimension, max_ppr ) VALUES (

'map_neatlines', 'neatline_gid',

'map_neatline_geom',1, 5, 2, 101);
