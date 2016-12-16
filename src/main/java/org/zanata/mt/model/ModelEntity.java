package org.zanata.mt.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@MappedSuperclass
public class ModelEntity implements Serializable {

    private static final long serialVersionUID = 8978601573586456188L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creationDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date lastChanged;

    public Date getCreationDate() {
        return new Date(creationDate.getTime());
    }

    public Date getLastChanged() {
        return new Date(lastChanged.getTime());
    }

    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @PrePersist
    public void onPersist() {
        Date now = new Date();
        if (this.creationDate == null) {
            this.creationDate = now;
        }
        if (this.lastChanged == null) {
            this.lastChanged = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.lastChanged = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelEntity)) return false;

        ModelEntity that = (ModelEntity) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (creationDate != null ? !creationDate.equals(that.creationDate) :
            that.creationDate != null) return false;
        return lastChanged != null ? lastChanged.equals(that.lastChanged) :
            that.lastChanged == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result =
            31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result =
            31 * result + (lastChanged != null ? lastChanged.hashCode() : 0);
        return result;
    }
}
