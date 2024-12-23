MATCH
(`x1`:A),
(`x2`:B),
(`x1`)-[:R]->(`x2`)
CREATE
(`x3`:C),
(`x2`)-[:S]->(`x3`)
RETURN *
