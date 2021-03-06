DROP FUNCTION IF EXISTS temporal_filter_common(text[], character varying,
				timestamp, timestamp, text);
CREATE OR REPLACE FUNCTION temporal_filter_common(tnames text[],
				    t_pred character varying,
				    tstart timestamp,
				    tend timestamp,
				    order_by_col TEXT
				    )
RETURNS TABLE(kb_archivid TEXT, event_starttime TIMESTAMP, event_endtime TIMESTAMP, hpc_boundcc TEXT
, hpc_coord TEXT, event_type TEXT, event_coordunit TEXT, hpc_bbox TEXT, orderby TIMESTAMP ) AS
$BODY$
DECLARE
    tname TEXT;
    t_query TEXT;

BEGIN
--Usage example:
--select temporal_filter_all(ARRAY['ar', 'ch']::TEXT[],
--	'GreaterThan', '2014-12-01 21:36:23', '2014-12-03 01:36:23', 'event_starttime');

--For testing:
--select kb_archivid, event_starttime from temporal_filter_common(ARRAY['ar', 'ch']::TEXT[],
--	'Precedes', '2014-12-01 21:36:23', '2014-12-03 01:36:23', 'event_starttime');


--Allowed temporal predicates are following:
--'Equals', 'LessThan', 'GreaterThan', 'Contains', 'ContainedBy',
--'Overlaps', 'Precedes', 'PrecededBy'

FOREACH tname IN ARRAY tnames
LOOP
   t_query = 'select * from temporal_filter_common_order( ' || quote_literal(tname);
   t_query = t_query ||', ' || quote_literal(t_pred) ||', ';
   t_query = t_query || quote_literal(tstart)||' , '|| quote_literal(tend) ;
   t_query = t_query || ', ' || quote_literal(order_by_col) ||')';
   RETURN QUERY EXECUTE t_query; --returns multiple times from different tables
END LOOP;

END;
$BODY$
LANGUAGE plpgsql;
