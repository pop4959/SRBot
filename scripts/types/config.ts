export default interface Config {
    plotly: {
        plotlyUsername: string;
        plotlyApikey: string;
    };
    seasons: Season[];
    rankColours: string[];
    ddApiRankUrl: string;
}

export interface Season {
    seasonName: string;
    seasonNumber: number;
    seasonRanks: SeasonRank[];
}

export interface SeasonRank {
    rankName: RankNames;
    rankPoints: number;
}

export enum RankNames {
    DIAMOND = 'diamond',
    PLATINUM = 'platinum',
    GOLD = 'gold',
    SILVER = 'silver',
    BRONZE = 'bronze',
    EXPERT = 'expert',
    ADVANCED = 'advanced',
    BEGINNER = 'beginner',
    ENTRY = 'entry',
};
