USE [SRO_VT_SHARD_INIT]
GO
/****** Object:  StoredProcedure [dbo].[_UPDATE_CHARSKILLS]    Script Date: 06.01.2015 05:06:44 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

/**
version : 2
author : Syloxx
created date: 2015-05-01
description : add all aviable skills to character.
return value :
0 = No Errors
100 = The charname does not exists.
101 = Unknown Error.
**/

CREATE PROCEDURE [dbo].[_UPDATE_CHARSKILLS]
	  @nvcCharName nvarchar(30)
AS
SET NOCOUNT ON
SET XACT_ABORT ON

DECLARE	  @intReturnValue int
		, @intCharID int
		, @intSkillID int

/**_# Rollback and return if inside an uncommittable transaction.*/
IF XACT_STATE() = -1
BEGIN
	SET @intReturnValue = 1
	GOTO ErrorHandler
END

BEGIN TRY
	SELECT	  @intCharID = CharID
	FROM	  _Char WITH (NOLOCK)
	WHERE	  CharName16 = @nvcCharName

	IF (@intCharID IS NULL OR @intCharID = 0)
	BEGIN
		SET @intReturnValue = 100
		GOTO ErrorHandler
	END

	/**_# [_CharSkill] DELETE FROM TABLE*/
	DELETE FROM _CharSkill WHERE CharID = @intCharID AND SkillID NOT IN (SELECT SkillID FROM _RefCharDefault_Skill)

	/**_# [UPDATE_SKILL_CURSOR] CREATE AND EXECUTE*/
	DECLARE UPDATE_SKILL_CURSOR CURSOR FOR
		SELECT	RS.ID
		FROM
			(
			SELECT	  Basic_Group
					, MAX(Basic_Level) AS Basic_Level
			FROM	  _RefSkill RS
			JOIN	  _CharSkillMastery CSM
			ON		  RS.ReqCommon_Mastery1 = CSM.MasteryID
			WHERE	  RS.Service = 1
			AND		  RS.ID NOT IN (SELECT SkillID FROM _RefCharDefault_Skill)
			AND		  RS.ReqCommon_MasteryLevel1 <= CSM.Level
			AND		  CSM.CharID = @intCharID
			GROUP BY  Basic_Group
			) MGS
		JOIN	  _RefSkill RS
		ON		  RS.Basic_Group = MGS.Basic_Group
		AND		  RS.Basic_Level = MGS.Basic_Level
		WHERE	  RS.ReqLearn_SP != 0

	OPEN UPDATE_SKILL_CURSOR
	FETCH NEXT FROM UPDATE_SKILL_CURSOR INTO @intSkillID
	WHILE @@FETCH_STATUS = 0
		BEGIN

			/**_# [_CharSkill] INSERT INTO TABLE*/
			INSERT INTO _CharSkill (CharID, SkillID, Enable)
			VALUES (@intCharID, @intSkillID, 1)

			FETCH NEXT FROM UPDATE_SKILL_CURSOR INTO @intSkillID
		END
	CLOSE UPDATE_SKILL_CURSOR
	DEALLOCATE UPDATE_SKILL_CURSOR
END TRY
BEGIN CATCH
	SET @intReturnValue = 101
	GOTO ErrorHandler
END CATCH

RETURN 0

ErrorHandler:
IF XACT_STATE() <> 0
	ROLLBACK TRANSACTION
	RETURN @intReturnValue