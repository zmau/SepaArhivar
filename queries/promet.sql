-- truncate table observation
select o.time, s.name station, c.name component, value
from observation o join station s on o.stationId = s.sepaId
	join component c on c.sepaId = o.componentId
where time > '2020-11-19'
order by stationId, time, componentId

select stationId, max(time)
from observation
group by stationId
order by max(time) desc