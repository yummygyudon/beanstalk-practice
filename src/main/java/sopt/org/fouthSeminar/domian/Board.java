package sopt.org.fouthSeminar.domian;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends AuditingTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private User user;

//    @Column
//    private String thumbnail;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Boolean isPublic;

    private Board(User user
//            , String thumbnail
            , String title
            , String content
            , Boolean isPublic
    ) {
        this.user = user;
//        this.thumbnail = thumbnail;
        this.title =title;
        this.content = content;
        this.isPublic = isPublic;
    }

    public static Board newInstance(
            User user
//            , String thumbnail
            , String title
            , String content
            , Boolean isPublic
    ) {
        return new Board(
                user
//                , thumbnail
                , title
                , content
                , isPublic
        );
    }
}