--Them cot cho table CODE_MASTER o db2
Delete from CODE_MASTER where cd_type = 'code.ratio.rating.ranking' and value = 6


Update CODE_MASTER
set Logical_name = 'Không đạt yêu cầu'
where cd_type = 'code.ratio.rating.ranking' and value = 1

Update CODE_MASTER
set Logical_name = 'Gần đạt yêu cầu'
where cd_type = 'code.ratio.rating.ranking' and value = 2


Update CODE_MASTER
set Logical_name = 'Đạt yêu cầu'
where cd_type = 'code.ratio.rating.ranking' and value = 3


Update CODE_MASTER
set Logical_name = 'Vượt yêu cầu'
where cd_type = 'code.ratio.rating.ranking' and value = 4


Update CODE_MASTER
set Logical_name = 'Vượt xa yêu cầu'
where cd_type = 'code.ratio.rating.ranking' and value = 5

